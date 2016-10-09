package network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import game.Container;
import game.Door;
import game.FinalRoom;
import game.Game;
import game.Game.itemType;
import game.ImmovableItem;
import game.InventoryItem;
import game.Item;
import game.Location;
import game.PuzzleRoom;
import game.Room;

/**
 * Responsible for testing the methods to be used in game for translating 
 * game state to clients and allowing them to interpret game state onto 
 * their local copy of the board.
 * 
 * Basically invented to  make sure I didn't fuck up the game class
 * 
 * @author Marielle
 *
 */
public class Parser {
	public Game game;
	
	//list of constant ints representing how many bytes each game element
	//is typically comprised of when they are sent from the master
	//this excludes the char that preceeds them
	private final int DOOR_BYTES = 6;
	private final int STORAGE_BYTES = 3;
	public final int MOVABLE_BYTES = 4;
	
	public Parser(Game game){
		this.game = game;
	}
	
	/**
	 * Reads a file from the game folder containing the initial game 
	 * state and converts it into an array of bytes which can then be 
	 * read by the client at the receiving end and converted back into
	 * game state data. 
	 *  
	 * @param file
	 * @return
	 */
	public static Room roomFromFile(Game game, File file)throws IOException{
		//create the file readers ready for parsing
		FileReader fr = new FileReader(file);
		BufferedReader in = new BufferedReader(fr);

		Room room = new PuzzleRoom(10);
		ArrayList<Container> containers = new ArrayList<Container>();
		
		ArrayList<String> lines = new ArrayList<String>();	//all lines in file
		String line; //current line in file
		int lineCount  = 0;
		
		//while the file has lines, process them
		while((line = in.readLine()) != null){
			lines.add(line);
			Scanner sc = new Scanner(line);
			
			//first line we need to process is line 12, for the room size
			if(lineCount == 11){
				char id = line.charAt(0);
				sc.next(); //skip char
				
				if(id == 'R'){
					room = new PuzzleRoom(sc.nextInt());
				}
				else if(id == 'F'){
					room = new FinalRoom(sc.nextInt());
				}
			}
			//process the items in the room
			else if(lineCount > 11){
				char id = line.charAt(0);
				sc.next(); //skip char
				//get one location as all items have at least one
				Location loc = new Location(sc.nextInt(), sc.nextInt());
				int itemID;
				itemType type;
				
				switch(id){
					case 'D':
						Door door = new Door(loc);
						room.addDoor(door);
						break;
					case 'C':
						itemID = sc.nextInt();
						type = game.itemCodes.get(itemID);
						Container cont = new Container(type, loc);
						containers.add(cont);
						room.setImmovableItem(cont, loc);
						break;
					case 'Q':
						itemID = sc.nextInt();
						type = game.itemCodes.get(itemID);
						InventoryItem invItem = new InventoryItem(type);
						invItem.setLocation(loc);
						//check whether this inventory item is in the same space as a container
						for(Container c: containers){
							if(c.getLocation().equals(loc)){
								c.addItem(invItem);
							}
						}
						//add item to room
						room.setInventoryItem(invItem, loc);
						break;
					case 'I':
						//process type
						itemID = sc.nextInt();
						type = game.itemCodes.get(itemID);
						ImmovableItem immItem = new ImmovableItem(type, loc);
						
						if(type != Game.itemType.COMPUTER || type != Game.itemType.DARKNESS 
								|| type != Game.itemType.CHAIR){
							//process additional locations it covers, as most immovables cover two locations
							Location secondary = new Location(sc.nextInt(), sc.nextInt());
							immItem.addToLocationsCovered(secondary);
							
							if(type == Game.itemType.TABLE){
								//table has three locations covered, so process additional one
								Location tertiary = new Location(sc.nextInt(), sc.nextInt());
								immItem.addToLocationsCovered(tertiary);
								
							}
						}
						room.setImmovableItem(immItem, loc);
						break;
					case 'P':
						room.addPSP(loc);
						break;						
				}
			}
			lineCount++;
		}
		
		return room;
	}
	
	public static byte[] stateToBytes() throws IOException{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);

		//dout.writeByte(game.getState());
		
		//first write the door updates
		return bout.toByteArray();
	}
	
	public synchronized void fromByteArray(byte[] bytes) throws IOException{
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		DataInputStream din = new DataInputStream(bin);
		
		//state = din.readByte();
		
		//read in the door object information
		//game.getDoors
	}
}
