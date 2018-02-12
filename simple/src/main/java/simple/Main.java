package simple;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

public class Main {
	public static void main(String[] args) throws Exception {
	    JDA jda = new JDABuilder(AccountType.BOT)
	        .setToken("BotToken")
	        .setGame(Game.listening(".help"))
	        .buildBlocking();

	    jda.addEventListener(new PlayerControl());
	   	  }

	
}
