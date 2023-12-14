import java.io.*;
import java.util.*;


public class Player {
	static BufferedWriter fileOut = null;
	
	/*
		GAME DATA
	*/
	public static int universeWidth;
	public static int universeHeight;
	public static String myColor;
	
	public static HashMap<String, Object>[] bluePlanets;
	public static HashMap<String, Object>[] cyanPlanets;
	public static HashMap<String, Object>[] greenPlanets;
	public static HashMap<String, Object>[] yellowPlanets;
	public static HashMap<String, Object>[] neutralPlanets;

	public static String[] blueFleets;
	public static String[] cyanFleets;
	public static String[] greenFleets;
	public static String[] yellowFleets;


	public static void main(String[] args) throws Exception {

		try {

			/*
				**************
				Main game loop
				**************
			  	- each iteration of the loop is one turn.
			  	- this will loop until we stop playing the game
			  	- we will be stopped if we die/win or if we crash
			*/
			while (true) {
				/*
					- at the start of turn we first recieve data
					about the universe from the game.
					- data will be loaded into the static variables of
					this class
				*/
				getGameState();


				//finding the smallest neutral planet
				float smallestSize = Float.MAX_VALUE;
				HashMap<String, Object> smallestNeutralPlanet = null;

				for(HashMap<String, Object> planet : neutralPlanets){
					float planetSize = (float) planet.get("planetSize");
					if (planetSize < smallestSize){
						smallestSize = planetSize;
						smallestNeutralPlanet = planet;
					}
				}

				//get name of the smallest neutral planet
				String planetNameSmallest = (String) smallestNeutralPlanet.get("name");





				/*
				 	*********************************
					LOGIC: figure out what to do with
					your turn
					*********************************
					- current plan: attack smallest neutral planet
				*/

				HashMap<String, Object>[] myPlanets = new HashMap[0];
				//defining myPlanets based on myColor
				switch (myColor) {
					case "blue":
						myPlanets = bluePlanets;
						break;
					case "cyan":
						myPlanets = cyanPlanets;
						break;
					case "green":
						myPlanets = greenPlanets;
						break;
					case "yellow":
						myPlanets = yellowPlanets;
						break;
					default:
						break;
				}


				//finding the nearest neutral planet
				HashMap<String, Object> nearestNeutralPlanet = null;
				int smallestDistance = Integer.MAX_VALUE;

				for (HashMap<String, Object> myPlanet : myPlanets) {
					int myPlanetX = (int) myPlanet.get("posX");
					int myPlanetY = (int) myPlanet.get("posY");

					for (HashMap<String, Object> neutralPlanet : neutralPlanets) {
						int neutralPlanetX = (int) neutralPlanet.get("posX");
						int neutralPlanetY = (int) neutralPlanet.get("posY");

						//Euclidean distance
						int distance = (int) Math.sqrt(Math.pow(neutralPlanetX - myPlanetX, 2) + Math.pow(neutralPlanetY - myPlanetY, 2));

						if (distance < smallestDistance) {
							smallestDistance = distance;
							nearestNeutralPlanet = neutralPlanet;
						}
					}
				}
				//get name of the nearest neutral planet
				String planetNameNearest = (String) nearestNeutralPlanet.get("name");



				//attacking the nearest neutral planet
				//will break once theres no neutral planets left
				if(myPlanets.length > 0){
					for (int i = 0 ; i < myPlanets.length ; i++) {
						HashMap<String, Object> myPlanet = myPlanets[i];
						String myPlanetName = (String) myPlanet.get("name");

						System.out.println("A " + myPlanetName + " " + planetNameNearest);
					}
				}

				/*
					- send a hello message to your teammate bot :)
					- it will recieve it form the game next turn (if the bot parses it)
				 */
				System.out.println("M Hello");

				/*
				  	- E will end my turn. 
				  	- you should end each turn (if you don't the game will think you timed-out)
				  	- after E you should send no more commands to the game
				 */
				System.out.println("E");
			}
		} catch (Exception e) {
			logToFile("ERROR: ");
			logToFile(e.getMessage());
			e.printStackTrace();
		}
		fileOut.close();
		
	}


	/**
	 * This function should be used instead of System.out.print for 
	 * debugging, since the System.out.println is used to send 
	 * commands to the game
	 * @param line String you want to log into the log file.
	 * @throws IOException
	 */
	public static void logToFile(String line) throws IOException {
		if (fileOut == null) {
			FileWriter fstream = new FileWriter("Igralec.log");
			fileOut = new BufferedWriter(fstream);
		}
		if (line.charAt(line.length() - 1) != '\n') {
			line += "\n";
		}
		fileOut.write(line);
		fileOut.flush();
	}


	/**
	 * This function should be called at the start of each turn to obtain information about the current state of the game.
	 * The data received includes details about planets and fleets, categorized by color and type.
	 *
	 * This version of the function uses dynamic lists to store data about planets and fleets for each color,
	 * accommodating for an unknown quantity of items. At the end of data collection, these lists are converted into fixed-size
	 * arrays for consistent integration with other parts of the program.
	 *
	 * Feel free to modify and extend this function to enhance the parsing of game data to your needs.
	 *
	 * @throws NumberFormatException if parsing numeric values from the input fails.
	 * @throws IOException if an I/O error occurs while reading input.
	 */
	public static void getGameState() throws NumberFormatException, IOException {
		BufferedReader stdin = new BufferedReader(
			new java.io.InputStreamReader(System.in)
		); 
		/*
			- this is where we will store the data recieved from the game,
			- Since we don't know how many planets/fleets each player will 
			have, we are using lists.
		*/
		LinkedList<HashMap<String, Object>> bluePlanetsList = new LinkedList<>();
		LinkedList<HashMap<String, Object>> cyanPlanetsList = new LinkedList<>();
		LinkedList<HashMap<String, Object>> greenPlanetsList = new LinkedList<>();
		LinkedList<HashMap<String, Object>> yellowPlanetsList = new LinkedList<>();
		LinkedList<HashMap<String, Object>> neutralPlanetsList = new LinkedList<>();

		LinkedList<String> blueFleetsList = new LinkedList<>();
		LinkedList<String> cyanFleetsList = new LinkedList<>();
		LinkedList<String> greenFleetsList = new LinkedList<>();
		LinkedList<String> yellowFleetsList = new LinkedList<>();

		
		/*
			********************************
			read the input from the game and
			parse it (get data from the game)
			********************************
			- game is telling us about the state of the game (who ows planets
			and what fleets/attacks are on their way). 
			- The game will give us data line by line. 
			- When the game only gives us "S", this is a sign
			that it is our turn and we can start calculating out turn.
			- NOTE: some things like parsing of fleets(attacks) is not implemented 
			and you should do it yourself
		*/
		String line = "";
		/*
			Loop until the game signals to start playing the turn with "S"
		*/ 
		while (!(line = stdin.readLine()).equals("S")) {
			/* 
				- save the data we recieve to the log file, so you can see what 
				data is recieved from the game (for debugging)
			*/ 
			logToFile(line); 
			
			String[] tokens = line.split(" ");
			char firstLetter = line.charAt(0);
			/*
			 	U <int> <int> <string> 						
				- Universe: Size (x, y) of playing field, and your color
			*/
			if (firstLetter == 'U') {
				universeWidth = Integer.parseInt(tokens[1]);
				universeHeight = Integer.parseInt(tokens[2]);
				myColor = tokens[3];
			} 
			/*
				P <int> <int> <int> <float> <int> <string> 	
				- Planet: Name (number), position x, position y, 
				planet size, army size, planet color (blue, cyan, green, yellow or null for neutral)
			*/

			if (firstLetter == 'P') {
				String planetName = tokens[1];
				int posX = Integer.parseInt(tokens[2]);
				int posY = Integer.parseInt(tokens[3]);
				float planetSize = Float.parseFloat(tokens[4]);
				int armySize = Integer.parseInt(tokens[5]);
				String planetColor = tokens[6];

				//Create a HashMap to store planet information
				Map<String, Object> planetInfo = new HashMap<>();
				planetInfo.put("name", planetName);
				planetInfo.put("posX", posX);
				planetInfo.put("posY", posY);
				planetInfo.put("planetSize", planetSize);
				planetInfo.put("armySize", armySize);
				planetInfo.put("planetColor", planetColor);

				//Depending on the color, add the planet to the corresponding list
				switch (planetColor) {
					case "blue":
						bluePlanetsList.add((HashMap<String, Object>) planetInfo);
						break;
					case "cyan":
						cyanPlanetsList.add((HashMap<String, Object>) planetInfo);
						break;
					case "green":
						greenPlanetsList.add((HashMap<String, Object>) planetInfo);
						break;
					case "yellow":
						yellowPlanetsList.add((HashMap<String, Object>) planetInfo);
						break;
					case "null":
						neutralPlanetsList.add((HashMap<String, Object>) planetInfo);
						break;
				}
			}

		}
		/*
			- override data from previous turn
			- convert the lists into fixed size arrays
		*/
		bluePlanets = bluePlanetsList.toArray(new HashMap[0]);
		cyanPlanets = cyanPlanetsList.toArray(new HashMap[0]);
		greenPlanets = greenPlanetsList.toArray(new HashMap[0]);
		yellowPlanets = yellowPlanetsList.toArray(new HashMap[0]);
		neutralPlanets = neutralPlanetsList.toArray(new HashMap[0]);
		blueFleets = blueFleetsList.toArray(new String[0]);
		cyanFleets = cyanFleetsList.toArray(new String[0]);
		greenFleets = greenFleetsList.toArray(new String[0]);
		yellowFleets = yellowFleetsList.toArray(new String[0]);
	}
}

