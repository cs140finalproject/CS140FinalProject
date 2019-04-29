package project;

public class DataAccessException extends RuntimeException{
	public DataAccessException(String s) {
		super(s); //call IllegalFormatFlagException with error message
	}
}