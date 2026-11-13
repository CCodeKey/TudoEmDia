package ted.com.tudoemdia.exception;

import org.springframework.http.HttpStatus;

// Erro previsto pelas regras de negócio; a mensagem é exibida ao usuário
public abstract class ApiException extends RuntimeException {

	private final HttpStatus status;

	protected ApiException(HttpStatus status, String mensagem) {
		super(mensagem);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
