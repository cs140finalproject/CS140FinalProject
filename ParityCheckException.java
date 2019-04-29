package project;

import java.util.IllegalFormatFlagsException;

//ParityCheckException is subclass of IllegalFormatFlagsException (unchecked error)
public class ParityCheckException extends IllegalFormatFlagsException{
	
	// Constructor
	public ParityCheckException(String s) {
		super(s); //call IllegalFormatFlagException with error message
	}
}
