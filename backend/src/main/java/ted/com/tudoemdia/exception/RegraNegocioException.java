package ted.com.tudoemdia.exception;

import org.springframework.http.HttpStatus;

public class RegraNegocioException extends ApiException {

	public RegraNegocioException(String mensagem) {
		super(HttpStatus.UNPROCESSABLE_CONTENT, mensagem);
	}
}
