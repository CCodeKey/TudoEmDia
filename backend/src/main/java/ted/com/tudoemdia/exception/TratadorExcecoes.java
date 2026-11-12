package ted.com.tudoemdia.exception;

import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Respostas de erro padronizadas (application/problem+json), sem detalhes internos
@RestControllerAdvice
public class TratadorExcecoes extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(TratadorExcecoes.class);

	@ExceptionHandler(ApiException.class)
	public ProblemDetail tratarApi(ApiException ex) {
		return ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
	}

	// Restrições do banco, como unicidade, também viram respostas claras (RNF09)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail tratarIntegridade(DataIntegrityViolationException ex) {
		log.warn("Operação recusada por restrição do banco: {}", ex.getClass().getSimpleName());
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A operação conflita com dados já existentes.");
	}

	@ExceptionHandler(DateTimeParseException.class)
	public ProblemDetail tratarData(DateTimeParseException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Data em formato inválido.");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail tratarInesperado(Exception ex) {
		log.error("Erro inesperado", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado.");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> campos = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos.");
		problema.setProperty("campos", campos);
		return ResponseEntity.badRequest().body(problema);
	}
}
