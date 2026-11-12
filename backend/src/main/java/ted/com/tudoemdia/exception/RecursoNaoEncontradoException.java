package ted.com.tudoemdia.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends ApiException {

	public RecursoNaoEncontradoException(String mensagem) {
		super(HttpStatus.NOT_FOUND, mensagem);
	}
}
