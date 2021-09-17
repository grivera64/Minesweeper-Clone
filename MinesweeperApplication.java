//package minesweeper;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

/* From tutorial at https://www.youtube.com/watch?v=JwcyxuKko_M */
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;
import java.util.ArrayList;

public class MinesweeperApplication extends Application
{
	
	/* Constants for the Minesweeper game */
	public static final int TILE_SIZE = 40;
	public static final int SCREEN_WIDTH = 800;
	public static final int SCREEN_HEIGHT = 600;
	
	/* Dimensions of the Tile Board */
	public static final int COLUMNS = SCREEN_WIDTH / TILE_SIZE;
	public static final int ROWS = SCREEN_HEIGHT / TILE_SIZE;
	
	/* Is the tile board/grid */
	private Tile[][] gameGrid = new Tile[COLUMNS][ROWS];
	
	/* Reference to the main scene for the Minesweeper game */
	private Scene mainScene;
	
	/* Represents if the player is still playing */
	private boolean isGameOver = false;
	
	/* Tile Object Class Definition */
	private class Tile extends StackPane
	{
		
		/* Tile attributes */
		private int xPos, yPos, bombCount;
		private boolean hasBomb;
		private Rectangle border = new Rectangle(TILE_SIZE - 2, TILE_SIZE - 2);
		private Text text = new Text();
		private boolean isOpened = false, isFlagged = false;
		
		/* Constructor */
		public Tile(int xPos, int yPos, boolean hasBomb)
		{
			
			/* Set values */
			this.xPos = xPos;
			this.yPos = yPos;
			this.hasBomb = hasBomb;
			
			/* Set-up the front end of the tile */
			this.border.setStroke(Color.LIGHTGREY);
			
			this.text.setFill(Color.BLACK);
			this.text.setFont(Font.font(18));
			this.text.setText((this.hasBomb) ? "X" : "");
			this.text.setVisible(false);
			
			this.getChildren().addAll(border, text);
			
			this.setTranslateX(this.xPos * TILE_SIZE);
			this.setTranslateY(this.yPos * TILE_SIZE);
			
			/* Create back end of Tile */
			this.setOnMouseClicked((e) -> 
			{
				
				/* Check which mouse button was pressed */
				var btnPressed = e.getButton();
				
				switch (btnPressed)
				{
				
					case PRIMARY:
						
						this.open();
						break;
						
					case SECONDARY:
						
						this.flag();
						break;
						
					default:
						
						return;
				
				}
				
			});
			
		}
		
		/* Open the current Tile */
		public void open()
		{
			
			/* Don't reopen */
			if (this.isOpened)
			{
				
				return;
				
			}
			
			/* Act differently if flagged */
			if (this.isFlagged)
			{

				/* Check if the game is over to reveal incorrect flags */
				if (isGameOver)
				{
					this.border.setFill(null);
					this.text.setFill(Color.BLACK);
					this.text.setFont(Font.font(18));
					
					/* Mark mis-flagged bombs as /'s */
					this.text.setText(this.hasBomb() ? "P": "/");
					
					this.text.setVisible(true);
					
				}
				
				/* Don't open flagged boxes otherwise */
				return;
				
			}
			
			/* Open the tile */
			this.isOpened = true;
			this.isFlagged = false;
			
			this.border.setFill(null);
			this.text.setFill(Color.BLACK);
			this.text.setFont(Font.font(18));
			
			this.text.setVisible(true);
			
			/* Open surrounding tiles if the tile has no bombs nearby */
			if (this.text.getText().isEmpty())
			{
				
				getNeighbors(this).forEach(Tile::open);
				
			}
			
			/* Detect a Game Over */
			if (this.hasBomb() && !isGameOver)
			{
				
				isGameOver = true;
				showGameOver("Game Over", "You touched a mine!");
				isGameOver = false;
				
				mainScene.setRoot(createContent());

			}
			
		}
		
		/* Set a flag */
		public void flag()
		{

			/* Don't flag opened tiles */
			if (this.isOpened)
			{
				
				return;
				
			}
			
			/* Remove flag from tile */
			if (this.isFlagged)
			{
				
				this.isFlagged = false;
			
				this.border.setFill(Color.BLACK);
				this.text.setFill(Color.BLACK);
				this.text.setFont(Font.font(18));
				this.text
				.setText(
					(this.hasBomb) ? ("X") : 
					(this.bombCount > 0) ? String.valueOf(this.bombCount): ""
				);
				this.text.setVisible(false);
				
			}
			/* Flag the tile */
			else
			{
				
				this.isFlagged = true;
				
				this.border.setFill(Color.BLACK);
				this.text.setFill(Color.WHITE);
				this.text.setFont(Font.font(18));
				this.text.setText("P");
				this.text.setVisible(true);
				
			}
			
		}
		
		/* Setters */
		
		/* Add indicators of bombs around if the current tile doesn't have any bombs */
		public void setIndicator(int bombCount)
		{
			
			this.bombCount = bombCount;
			this.text.setText((bombCount > 0) ? String.valueOf(bombCount) : "");
			
		}
		
		/* Getters */
		
		public int getXPos()
		{
			
			return this.xPos;
			
		}
		
		public int getYPos()
		{
			
			return this.yPos;
			
		}
		
		public boolean hasBomb()
		{
			
			return hasBomb;
			
		}
		
	}
	
	/* 
	 * 
	 * Creates a Game Over pop up and shows
	 * the rest of the tiles that weren't open
	 * 
	 * With help from 
	 * https://stackoverflow.com/questions/26341152/controlsfx-dialogs-deprecated-for-what/
	 * 32618003#32618003
	 * 
	 */
	public void showGameOver(String title, String message) 
	{
        
		/* Open all of the tiles */
		for (Tile[] col : gameGrid)
		{
			
			for (Tile tile : col)
			{
				
				tile.open();
				
			}
			
		}
		
		/* Set up an alert to the screen */
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        /* Show the alert until exited */
        alert.showAndWait();
        
    }
	
	/* Locates the neighbors around the given tile */
	private List<Tile> getNeighbors(Tile tile)
	{
		
		List<Tile> neighbors = new ArrayList<Tile>();
		
		/*
		 * 
		 * Columns
		 * 1 2 3
		 * N N N
		 * N T N
		 * N N N
		 * 
		 */
		
		int[] points = new int[]
		{
		
			/* Left Column */
			-1, -1,
			-1, 0,
			-1, 1,
			
			/* Middle Column */
			0, -1,
			0, 1,
			
			/* Right Column */
			1, -1,
			1, 0,
			1, 1
						
		};
		
		/* Add each possible neighbor into returned list */
		int dx, dy, newXPos, newYPos;
		
		for (int i = 0; i < points.length - 1; i++)
		{
			
			/* Represents the change in x and y */
			dx = points[i];
			dy = points[++i];
			
			/* Represents the coordinate of a possible square */
			newXPos = tile.getXPos() + dx;
			newYPos = tile.getYPos() + dy;
			
			/* Add to neighbors list if it is a valid grid space */
			if ((newXPos >= 0 && newXPos < COLUMNS) && (newYPos >= 0 && newYPos < ROWS))
			{
			
				neighbors.add(gameGrid[newXPos][newYPos]);
				
			}
			
		}
		
		return neighbors;
		
	}
	
	/* Creates the scene for Minesweeper */
	public Parent createContent()
	{
		
		/* Setup root pane for placing tiles on */
		Pane rootPane = new Pane();
		rootPane.setPrefSize(800, 600);
		
		/* Create tiles */
		Tile tmpTile;
		for (int currRow = 0; currRow < ROWS; currRow++)
		{
			
			for (int currCol = 0; currCol < COLUMNS; currCol++)
			{
				
				tmpTile = new Tile(currCol, currRow, Math.random() < 0.2f);
				
				this.gameGrid[currCol][currRow] = tmpTile;
				rootPane.getChildren().add(tmpTile);
				
			}
			
		}
		
		/* Set the indicators for each tile of where other bombs are */
		for (int currRow = 0; currRow < ROWS; currRow++)
		{
			
			for (int currCol = 0; currCol < COLUMNS; currCol++)
			{
				
				tmpTile = this.gameGrid[currCol][currRow];
				
				if (tmpTile.hasBomb()) continue;
				
				/* Set Bombs */
				long bombsCount = 
						this.getNeighbors(tmpTile)
						.stream().filter((tile) -> tile.hasBomb()).count();
				
				tmpTile.setIndicator((int) bombsCount);
				
			}
			
		}
		
		/* Return the pane (Parent Class is Parent) */
		return rootPane;
		
	}
	
	/* TODO: Add a winning screen */
	public void showWinningScreen()
	{
		
		// TODO: Add winning Screen Code
		
	}
	
	/* JavaFX Method that sets up the primaryStage */
	//@Override
	public void start(Stage primaryStage)
	{
		
		/* Create main scene */
		this.mainScene = new Scene(this.createContent());
		
		/* Display main scene */
		primaryStage.setTitle("Minesweeper in Java");
		primaryStage.setScene(mainScene);
		primaryStage.show();
		
	}
	
	/* Driver Function */
	public static void main(String[] args)
	{
		
		launch(args);
		
	}
	
}