package ted.com.tudoemdia.exception;

import org.springframework.http.HttpStatus;

public class NaoAutenticadoException extends ApiException {

	public NaoAutenticadoException() {
		this("Faça login para continuar.");
	}

	public NaoAutenticadoException(String mensagem) {
		super(HttpStatus.UNAUTHORIZED, mensagem);
	}
}
