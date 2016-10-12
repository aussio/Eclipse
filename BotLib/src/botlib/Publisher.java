package botlib;

public interface Publisher {
	public void notify_subscribers();
	public void attach(Subscriber... subscribers);
}
