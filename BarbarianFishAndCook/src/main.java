import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
 
import java.awt.*;
 
@ScriptManifest(author = "You", info = "My first script", name = "Tea thiever", version = 0, logo = "")
public class main extends Script {
 
    @Override
    public void onStart() {
        log("Let's get started!");
    }
 
    @Override
    public int onLoop() throws InterruptedException {
        return random(200, 300);
    }
 
    @Override
    public void onExit() {
        log("Thanks for running my Tea Thiever!");
    }
 
    @Override
    public void onPaint(Graphics2D g) {
 
    }
 
}