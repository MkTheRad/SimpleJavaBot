package simple;


import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
public class PlayerControl extends ListenerAdapter
{
	
	public static final String HELP="``Prefix for all commands: .``\n" + "\n"+
			"``Example:  .play``\n" + "\n"+
			"``join [name]  - Joins a voice channel that has the provided name``\n" +  "\n"+
			"``leave  - Leaves the voice channel that the bot is currently in.``\n" + "\n"+
			"``play   - Plays songs from the current complete. Starts playing again if it was previously paused``\n" + "\n"+
			"``pplay  - Adds a playlist to the complete and starts playing if not already playing``\n" + "\n"+
			"``pause  - Pauses audio playback``\n" + "\n"+
			"``stop   - Completely stops audio playback, skipping the current song.``\n" + "\n"+
			"``skip   - Skips the current song, automatically starting the next``\n" + "\n"+
			"``nowplaying   - Prints information about the currently playing song (title, current time)``\n" + "\n"+
			"``np    - alias for nowplaying\n``" + "\n"+
			"``list   - Lists the songs in the complete``\n" + "\n"+
			"``volume [val] - Sets the volume of the MusicPlayer [10 - 100]\n``" + "\n"+
			"``restart - Restarts the current song or restarts the previous song if there is no current song playing.``\n" + "\n"+
			"``repeat  - Makes the player repeat the currently playing song\n``" + "\n"+
			"``reset   - Completely resets the player, fixing all errors and clearing the complete.``";
    public static final int DEFAULT_VOLUME = 35; //(0 - 150, where 100 is default max volume)

    private final DefaultAudioPlayerManager playerManager;
    private final Map<String, GuildMusicManager> musicManagers;

    public PlayerControl()
    {

        this.playerManager = new DefaultAudioPlayerManager();
        
        YoutubeAudioSourceManager youtubemanager = new YoutubeAudioSourceManager();
        
       playerManager.registerSourceManager(youtubemanager);
        AudioSourceManagers.registerRemoteSources(playerManager);
        	playerManager.createPlayer();
        
     

        musicManagers = new HashMap<String, GuildMusicManager>();
    }

    
   
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
   
    	 if(event.isFromType(ChannelType.TEXT)) {
    		 
    		  String[] command = event.getMessage().getContentDisplay().split(" ", 2);
    	        if (!command[0].startsWith("."))    //message doesn't start with prefix.
    	            return;

    	        Guild guild = event.getGuild();
    	        GuildMusicManager mng = getMusicManager(guild);
    	        AudioPlayer player = mng.player;
    	        TrackScheduler scheduler = mng.scheduler;
    	    	VoiceChannel channel = guild.getMember(event.getAuthor()).getVoiceState().getChannel();

    	    	
    	 if (".join".equals(command[0]))
       {
          if (command.length == 1)// No channel name was provided to search for.
          {
                event.getChannel().sendMessage("No channel name was provided to search with to join.").complete();
            }
            else
            {
                VoiceChannel chan = null;
                try
                {
                     chan = guild.getVoiceChannelById(command[1]);
                }
                catch (NumberFormatException ignored) {}

                if (chan == null)
                    chan = guild.getVoiceChannelsByName(command[1], true).stream().findFirst().orElse(null);
                if (chan == null)
                {
                    event.getChannel().sendMessage("Could not find VoiceChannel by name: " + command[1]).complete();
                }
                else
                {
                    guild.getAudioManager().setSendingHandler(mng.getSendHandler());

                    try
                    {
                        guild.getAudioManager().openAudioConnection(chan);
                        event.getChannel().sendMessage("Succesfully Joined "+chan.getName()).complete();
                    }
                    catch (PermissionException e)
                    {
                        if (e.getPermission() == Permission.VOICE_CONNECT)
                        {
                            event.getChannel().sendMessage("Rhyno does not have permission to connect to: " + chan.getName()).complete();
                        }
                    }
               }
            }
        }
        else if (".leave".equals(command[0])&& guild.getAudioManager().isConnected())
        {
            guild.getAudioManager().setSendingHandler(null);
            guild.getAudioManager().closeAudioConnection();
            event.getChannel().sendMessage("Disconnecting...").complete();
        }
        else if (".play".equals(command[0])) {
        	
        	//add stuff here to disconeect when channel is empty
        	while(guild.getAudioManager().isConnected()) {
        		if(true) {
        			
        		}
        	}
        
            if (command.length == 1) //It is only the command to start playback (probably after pause)
            {
                if (player.isPaused())
                {
                    player.setPaused(false);
                    event.getChannel().sendMessage("Playback as been resumed.").complete();
                }
                else if (player.getPlayingTrack() != null)
                {
                    event.getChannel().sendMessage("Player is already playing!").complete();
                }
                else if (scheduler.queue.isEmpty() || guild.getAudioManager().isConnected())
                {
                    event.getChannel().sendMessage("Rhyno was not connected to channel , Please connect to a channel to queue tracks").complete();
                }
                else if(scheduler.queue.isEmpty())
                {
                	event.getChannel().sendMessage("The current audio queue is empty! Add something to the queue first!").complete();
                }
            	}
            else   // Commands has 2 parts, .play and url	
                {
            	
            	if(channel == null) event.getChannel().sendMessage("Please,Connect To A Voice Channel First").complete();
            	else {
            		guild.getAudioManager().setSendingHandler(mng.getSendHandler());
            		try {
            			guild.getAudioManager().openAudioConnection(channel);
            			

                        
                      

          				
                    	loadAndPlay(mng, event.getChannel(), "ytsearch:"+command[1], false);
                    
            			}catch(PermissionException e) {
            			if(e.getPermission()==Permission.VOICE_CONNECT) {
            				event.getChannel().sendMessage("Rhyno Can't Connect To"+channel.getName()).complete();
            			}
            		}
            	}
                }       
        }
       else if (".skip".equals(command[0]))
        {
            mng.scheduler.nextTrack();
            event.getChannel().sendMessage("The current track was skipped.").complete();
        }
        else if (".pause".equals(command[0]))
        {
            if (player.getPlayingTrack() == null)
            {
                event.getChannel().sendMessage("Cannot pause or resume player because no track is loaded for playing.").complete();
                return;
            }

            player.setPaused(!player.isPaused());
            if (player.isPaused())
                event.getChannel().sendMessage("The player has been paused.").complete();
            else
                event.getChannel().sendMessage("The player has resumed playing.").complete();
        }
        else if (".stop".equals(command[0]))
        {
            scheduler.queue.clear();
            player.stopTrack();
            player.setPaused(false);
            event.getChannel().sendMessage("Playback has been completely stopped and the queue has been cleared.").complete();
        }
        else if (".volume".equals(command[0]))
        {
            if (command.length == 1)
            {
                event.getChannel().sendMessage("Current player volume: **" + player.getVolume() + "**").complete();
            }
            else
            {
                try
                {
                    int newVolume = Math.max(10, Math.min(100, Integer.parseInt(command[1])));
                    int oldVolume = player.getVolume();
                    player.setVolume(newVolume);
                    event.getChannel().sendMessage("Player volume changed from `" + oldVolume + "` to `" + newVolume + "`").complete();
                }
                catch (NumberFormatException e)
                {
                    event.getChannel().sendMessage("`" + command[1] + "` is not a valid integer. (10 - 100)").complete();
                }
            }
        }
        else if (".restart".equals(command[0]))
        {
            AudioTrack track = player.getPlayingTrack();
            if (track == null)
                track = scheduler.lastTrack;

            if (track != null)
            {
                event.getChannel().sendMessage("Restarting track: " + track.getInfo().title).complete();
                player.playTrack(track.makeClone());
            }
            else
            {
                event.getChannel().sendMessage("No track has been previously started, so the player cannot replay a track!").complete();
            }
        }
        else if (".repeat".equals(command[0]))
        {
            scheduler.setRepeating(!scheduler.isRepeating());
            event.getChannel().sendMessage("Player was set to: **" + (scheduler.isRepeating() ? "repeat" : "not repeat") + "**").complete();
        }
        else if (".reset".equals(command[0]))
        {
            synchronized (musicManagers)
            {
                scheduler.queue.clear();
                player.destroy();
                guild.getAudioManager().setSendingHandler(null);
                musicManagers.remove(guild.getId());
            }

            mng = getMusicManager(guild);
            guild.getAudioManager().setSendingHandler(mng.getSendHandler());
            event.getChannel().sendMessage("The player has been completely reset!").complete();

        }
        else if (".nowplaying".equals(command[0]) || ".np".equals(command[0]))
        {
            AudioTrack currentTrack = player.getPlayingTrack();
            if (currentTrack != null)
            {
                String title = currentTrack.getInfo().title;
                String position = getTimestamp(currentTrack.getPosition());
                String duration = getTimestamp(currentTrack.getDuration());

                String nowplaying = String.format("**Playing:** %s\n**Time:** [%s / %s]",
                        title, position, duration);

                event.getChannel().sendMessage(nowplaying).complete();
            }
            else
                event.getChannel().sendMessage("The player is not currently playing anything!").complete();
        }
        else if (".list".equals(command[0]))
        {
            Queue<AudioTrack> queue = scheduler.queue;
            synchronized (queue)
            {
                if (queue.isEmpty())
                {
                    event.getChannel().sendMessage("The queue is currently empty!").complete();
                }
                else
                {
                    int trackCount = 0;
                    long completeLength = 0;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Current queue: Entries: ").append(queue.size()).append("\n");
                    for (AudioTrack track : queue)
                    {
                        completeLength += track.getDuration();
                        if (trackCount < 10)
                        {
                            sb.append("`[").append(getTimestamp(track.getDuration())).append("]` ");
                            sb.append(track.getInfo().title).append("\n");
                            trackCount++;
                        }
                    }
                    sb.append("\n").append("Total queue Time Length: ").append(getTimestamp(completeLength));

                    event.getChannel().sendMessage(sb.toString()).complete();
                }
            }
        }
        else if (".shuffle".equals(command[0]))
        {
            if (scheduler.queue.isEmpty())
            {
                event.getChannel().sendMessage("The queue is currently empty!").complete();
                return;
            }

            scheduler.shuffle();
            event.getChannel().sendMessage("The queue has been shuffled!").complete();
        }
        else if(".help".equals(command[0])) { event.getAuthor().openPrivateChannel().queue(q->q.sendMessage(HELP).complete());
        event.getChannel().sendMessage(event.getAuthor().getAsMention()+"Help Is On The Way!!").complete();
          }
        }
    	 else {
    		 
    		 return;
    		 
}
    }

    private void loadAndPlay(GuildMusicManager mng, final MessageChannel channel, String url, final boolean addPlaylist)
    {
        final String trackUrl;
        
      //  Strip <>'s that prevent discord from embedding link resources
       if (url.startsWith("<") && url.endsWith(">"))
            trackUrl = url.substring(1, url.length() - 1);
       else
            trackUrl = url;

        playerManager.loadItemOrdered(mng , trackUrl, new AudioLoadResultHandler()
        {
           @Override
           public void trackLoaded(AudioTrack track)
           {
        	   	
                String msg = "Adding to queue: " + track.getInfo().title;
               if (mng.player.getPlayingTrack() == null)
                    msg += "\nand the Player has started playing;";
           	
               mng.scheduler.queue(track);
            channel.sendMessage(msg).queue();
            
         }

             @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
            	 
            AudioTrack firstTrack = playlist.getSelectedTrack();
               List<AudioTrack> tracks = playlist.getTracks();
               
           
               
               
          if(firstTrack==null) {
        	  firstTrack = tracks.get(0); 
          }
                		
                
           
               if (addPlaylist)
               {
                   channel.sendMessage("Adding **" + playlist.getTracks().size() +"** tracks to queue from playlist: " + playlist.getName()).queue();
                    tracks.forEach(mng.scheduler::queue);
                }
              
                else
               {
                	
                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue((v)->{ });
                			
                         mng.scheduler.queue(firstTrack);
                }
           }
    
            @Override
            public void noMatches()
             {
                channel.sendMessage("Nothing found by " + trackUrl).complete();
            }

          @Override
           public void loadFailed(FriendlyException exception)
           {
               channel.sendMessage("Could not play: " + exception.getMessage()).complete();
           }
       });
     }

    private GuildMusicManager getMusicManager(Guild guild)
    {
        String guildId = guild.getId();
        GuildMusicManager mng = musicManagers.get(guildId);
        if (mng == null)
        {
            synchronized (musicManagers)
            {
                mng = musicManagers.get(guildId);
                if (mng == null)
                {
                    mng = new GuildMusicManager(playerManager);
                    mng.player.setVolume(DEFAULT_VOLUME);
                    musicManagers.put(guildId, mng);
                }
            }
        }
        return mng;
    }

    private static String getTimestamp(long milliseconds)
    {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours   = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

}