package pop3;

public abstract class Restriction <T>{
	abstract boolean validateRestriction(T data);
}
