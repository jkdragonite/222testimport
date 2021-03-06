
package game;

import java.util.ArrayList;
import java.util.HashMap;

import jdk.internal.dynalink.beans.StaticClass;

import com.sun.org.apache.bcel.internal.generic.INEG;

public class Game {
	private viewDirection view;
//	private renderRoom currentRoom;
	public ArrayList<Room> rooms = new ArrayList<Room>();
	public HashMap<Integer, itemType> itemCodes = new HashMap<>();
	

	
	public ArrayList<Player> players = new ArrayList<Player>();
		
	private int state;		//indicates the current state of the game
	public static final int WAITING = 0;
	public static final int READY = 1;
	public static final int PLAYING = 2;
	public static final int GAMEWON = 3;
	
	/**
	 * indicates the players current view direction, i.e the
	 * perspective to render from
	 * 
	 * @author Jordan
	 *
	 */
	public enum viewDirection {
		NORTH, SOUTH, EAST, WEST
	}
	
	/**
	 * indicates the player's current room i.e the room to render
	 * 
	 * @author Jordan
	 *
	 */
	public enum renderRoom {
		ROOM1, ROOM2, ROOM3, ROOM4, FINALROOM
	}
	
	
	/**
	 * 
	 * enum type for items
	 * 
	 * @author Jordan
	 *
	 */
	public enum itemType {
		BOX, BOOKSHELF, BOOK, BED, CHAIR,
		COMPUTER, DARKNESS, DESK, KEY, TABLE
	}
	
	
	public Game() {
		this.view = viewDirection.NORTH;	
//		this.currentRoom = renderRoom.ROOM1;
		initializeItemsCodes();
		
		//testing only pls remove
		rooms.add(new PuzzleRoom(10));
		Location itemLocation = new Location(0, 1);
		Location movableItemLocation = new Location(9, 9);
		Location immovableItemLocation = new Location(4, 4);
//		rooms.get(0).board.getSquare(itemLocation).setInventory(new InventoryItem(itemType.KEY, itemLocation, "Key"));
		addInventoryItemToGame(new InventoryItem(itemType.KEY, itemLocation, "Key"), 0);
		ImmovableItem bookShelf = new ImmovableItem(itemType.BOOKSHELF, immovableItemLocation);
		bookShelf.addToLocationsCovered(new Location(5, 5));
		addImmovableItemToGame(bookShelf, 0);
//		rooms.get(0).board.getSquare(immovableItemLocation).setImmovableItem(new ImmovableItem(itemType.BOOKSHELF, immovableItemLocation));
		rooms.get(0).board.getSquare(movableItemLocation).setMovableItem(new MovableItem(itemType.BOX, movableItemLocation));
		addPlayer(200);
		this.getPlayer(200).getRoom().updatePlayerMoves(this.getPlayer(200));
//		players.add(new Player(200, rooms.get(0)));
	}
	
	public void initializeItemsCodes(){
		this.itemCodes.put(610, itemType.BOX);
		this.itemCodes.put(611, itemType.BOOKSHELF);
		this.itemCodes.put(612, itemType.BOOK);
		this.itemCodes.put(614, itemType.BED);
		this.itemCodes.put(615, itemType.CHAIR);
		this.itemCodes.put(616, itemType.COMPUTER);
		this.itemCodes.put(617, itemType.DARKNESS);
		this.itemCodes.put(618, itemType.DESK);
		this.itemCodes.put(619, itemType.KEY);
		this.itemCodes.put(620, itemType.TABLE);	
	}
	
	public void shiftView(viewDirection direction, int uid){
		getPlayer(uid).shiftView(direction);
//		this.view = direction;
	}
	
//	public void changeRoom(renderRoom room){
//		this.currentRoom = room;
//	}
	
	public Player getPlayer(int uid){
		return this.players.get(uid - 200);
	}
	
	
	public void addInventoryItemToGame(InventoryItem item, int roomNumber){
		this.rooms.get(roomNumber).inventoryItems.add(item);
		this.rooms.get(roomNumber).setInventoryItem(item, item.getLocation());
	}
	
	public void addMovableItemToGame(MovableItem item, int roomNumber){
		this.rooms.get(roomNumber).movableItems.add(item);
		this.rooms.get(roomNumber).setMovableItem(item, item.getLocation());
	}
	
	public void addImmovableItemToGame(ImmovableItem item, int roomNumber){
		if (item instanceof Container){
			this.rooms.get(roomNumber).containers.add((Container) item);
		}
		else{
			this.rooms.get(roomNumber).immovableItems.add(item);
		}
		
		this.rooms.get(roomNumber).setImmovableItem(item, item.getLocation());
	}
	
	
	
	public void addPlayer(int uid){
		Player playerToAdd = new Player(uid, this.rooms.get(0));
		this.players.add(playerToAdd);
//		System.out.println(playerToAdd.getLocation().toString());
//		System.out.println(this.rooms.get(0).board.getSquare(playerToAdd.getLocation()));
		this.rooms.get(0).board.getSquare(playerToAdd.getLocation()).addPlayer(playerToAdd);
		this.rooms.get(0).addPlayer(playerToAdd);
//		System.out.println(this.rooms.get(0).board.getSquare(playerToAdd.getLocation()).getPlayer().getLocation().toString());
		
	}
		

	/**
	 * retrieves enum current view direction for use by the renderer
	 * 
	 * @return enum viewdirection
	 */
	public viewDirection getDirection(int uid){
		return this.getPlayer(200).getView();
	}
	
	/**
	 * Gets the current state of the game based on the integer constants.
	 * @return 
	 */
	public int getState(){
		return state;
	}
	
	/**
	 * Sets the current game state to the specified integer.
	 * @param state
	 */
	public void setState(int state){
		this.state = state;
	}
//	/**
//	 * returns current room the player is in for use by the renderer
//	 * 
//	 * @return enum renderRoom
//	 */
//	public renderRoom getRoom(){
//		return currentRoom;
//	}
	
	/**
	 * returns a list of room's doors
	 * 
	 * @return arraylist door
	 */
	public ArrayList<Door> getDoors(){
		ArrayList<Door> doors = new ArrayList<Door>();
		for (Room room : this.rooms){
			doors.add(room.door);
		}
		return doors;
	}
	
	/**
	 * returns a list of rooms the players are currently in
	 * @return arraylist rooms
	 */
	public ArrayList<Room> occupiedRooms(){
		ArrayList<Room> rooms = new ArrayList<Room>();
		for (Player player : this.players){
			rooms.add(player.getRoom());
		}
		return rooms;
	}
	
	public void setDestinationRooms(){
		int count = 1;
		for (Room room : this.rooms){
			if (room instanceof PuzzleRoom){
				if (count < this.rooms.size()){
					room.door.setDestination(this.rooms.get(count));
				}					
			}
			count++;
		}
	}
	
}
