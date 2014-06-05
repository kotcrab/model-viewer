/*******************************************************************************
 * Copyright 2013 Pawel Pastuszak
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package pl.kotcrab.modelviewer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;

public class ModelViewer
{
	
	private JFrame frame;
	final JFileChooser fc = new JFileChooser();
	
	/**
	 * Curent model path
	 */
	private String currentPath = null;
	/**
	 * List of waiting events (added by frame, processed by renderer)
	 */
	public static final ArrayList<Event> eventList = new ArrayList<Event>();
	
	private boolean optRenderLines = true;
	private boolean optRenderText = true;
	private boolean optRenderLight = true;
	
	private JSpinner animSpinner;
	/**
	 * Stop animation
	 */
	private JMenuItem btnStop;
	/**
	 * Play animation
	 */
	private JMenuItem btnPlay;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		//change lool and feel
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					ModelViewer window = new ModelViewer();
					window.frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the application.
	 */
	public ModelViewer()
	{
		initialize();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frame = new JFrame("Libgdx 3D Model Viewer");
		
		frame.setBounds(100, 100, 659, 451);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel renderPanel = new JPanel(new BorderLayout());
		frame.getContentPane().add(renderPanel, BorderLayout.CENTER);
		
		final LwjglCanvas canvas = new LwjglCanvas(new Renderer(), true); //create canvas used for rendering
		renderPanel.add(canvas.getCanvas()); //atach canvas to panel
		
		JMenuBar menuBar = new JMenuBar();
		
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);
		
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("OBJ or G3DB model", "obj", "OBJ", "g3db", "G3DB"));
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Load");
		mntmNewMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int returnVal = fc.showOpenDialog(frame);
				
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					eventList.add(new Event(fc.getSelectedFile().getAbsolutePath().replace('\\', '/'), EventType.CHANGEFILE));
				}
				
			}
		});
		mnFileMenu.add(mntmNewMenuItem);
		
		JMenu mnRenderMenu = new JMenu("Render");
		menuBar.add(mnRenderMenu);
		
		final JCheckBoxMenuItem chckRenderLines = new JCheckBoxMenuItem("Lines");
		chckRenderLines.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent e)
			{
				optRenderLines = chckRenderLines.isSelected();
			}
		});
		
		final JCheckBoxMenuItem chckRenderLight = new JCheckBoxMenuItem("Light");
		chckRenderLight.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optRenderLight = chckRenderLight.isSelected();
			}
		});
		
		final JCheckBoxMenuItem chckRenderText = new JCheckBoxMenuItem("Text");
		chckRenderText.addActionListener(new ActionListener()
		{
			
			public void actionPerformed(ActionEvent e)
			{
				optRenderText = chckRenderText.isSelected();
			}
		});
		
		chckRenderLight.setSelected(true);
		chckRenderLines.setSelected(true);
		chckRenderText.setSelected(true);
		mnRenderMenu.add(chckRenderLight);
		mnRenderMenu.add(chckRenderLines);
		mnRenderMenu.add(chckRenderText);
		
		JMenu mnAnimation = new JMenu("Animation");
		menuBar.add(mnAnimation);
		
		animSpinner = new JSpinner();
		mnAnimation.add(animSpinner);
		
		btnPlay = new JMenuItem("Play");
		btnPlay.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				eventList.add(new Event(null, EventType.PLAYANIM));
			}
		});
		mnAnimation.add(btnPlay);
		
		btnStop = new JMenuItem("Stop");
		btnStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				eventList.add(new Event(null, EventType.STOPANIM));
				
			}
		});
		mnAnimation.add(btnStop);
		
		JMenu mnModel = new JMenu("Model");
		menuBar.add(mnModel);
		
		JMenuItem btnModelScale = new JMenuItem("Scale");
		btnModelScale.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				float scale;
				try
				{
					scale = Float.parseFloat(JOptionPane.showInputDialog(null, "Please enter new scale: "));
				}
				catch (NumberFormatException e1) //if entered text is not number
				{
					JOptionPane.showMessageDialog(null, "Enter number!");
					return;
				}
				
				if (scale <= 0)
				{
					JOptionPane.showMessageDialog(null, "Wrong number!");
					return;
				}
				
				eventList.add(new Event("" + scale, EventType.SCALE));
			}
		});
		mnModel.add(btnModelScale);
		
	}
	
	/**
	 * Rendering class
	 *
	 */
	class Renderer implements ApplicationListener
	{
		//camera
		private PerspectiveCamera cam;
		private CameraInputController camController;
		
		//models
		private ModelBatch modelBatch;
		private ModelInstance instance;
		private AssetManager assets;
		
		private boolean loading;

		//things for 2d rendering
		private SpriteBatch batch;
		private Matrix4 viewMatrix;
		private BitmapFont font;

		//lines
		private ModelBuilder modelBuilder;
		private ModelInstance xi;
		private ModelInstance yi;
		private ModelInstance zi;
		private Model x;
		private Model y;
		private Model z;
		
		private Environment lights;

		//animation
		private AnimationController animContrl;
		private boolean animPlaying;
		
		@Override
		public void create()
		{
			modelBatch = new ModelBatch();
			
			lights = new Environment();
			lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
			lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -10f, -8f, -2f));
			
			cam = new PerspectiveCamera(67, 400, 300);
			cam.position.set(1f, 1f, 1f);
			cam.lookAt(0, 0, 0);
			cam.near = 0.1f;
			cam.far = 300f;
			cam.update();
			
			camController = new CameraInputController(cam);
			Gdx.input.setInputProcessor(camController);
			
			//2d stuff
			batch = new SpriteBatch();
			viewMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());			
			font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), Gdx.files.internal("data/arial-15_00.png"), false);
			
			//load startup model
			assets = new AssetManager();
			assets.load("data/ship.obj", Model.class);
			currentPath = "data/ship.obj";
			loading = true;
			
			//lines 
			modelBuilder = new ModelBuilder();
			
			x = modelBuilder.createBox(0.01f, 0.01f, 300f, new Material(ColorAttribute.createDiffuse(Color.BLUE)), Usage.Position | Usage.Normal);
			xi = new ModelInstance(x);
			xi.transform.setToTranslation(0, 0, 150);
			
			y = modelBuilder.createBox(0.01f, 0.01f, 300f, new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position | Usage.Normal);
			yi = new ModelInstance(y);
			Matrix4 transform = new Matrix4().setToTranslation(150, 0, 0);
			Matrix4 rotate = new Matrix4().setToRotation(0, 1, 0, 90);
			yi.transform = transform.mul(rotate);
			
			z = modelBuilder.createBox(0.01f, 0.01f, 300f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal);
			zi = new ModelInstance(z);
			transform = new Matrix4().setToTranslation(0, 150, 0);
			rotate = new Matrix4().setToRotation(1, 0, 0, 90);
			zi.transform = transform.mul(rotate);
			
		}
		
		/**
		 * Assets manager finished loading models
		 */
		private void doneLoading()
		{
			Model newModel = assets.get(currentPath, Model.class);
			instance = new ModelInstance(newModel);
			loading = false;
			
			//setting spinner max values
			if (instance.animations.size == 0) //0 if no animation in loaded model
				animSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
			else
				animSpinner.setModel(new SpinnerNumberModel(0, 0, instance.animations.size - 1, 1));
			
			//if thers no animation deactivte play and stop button
			if (instance.animations.size == 0)
			{
				btnPlay.setEnabled(false);
				btnStop.setEnabled(false);
			}
			else
			{
				btnPlay.setEnabled(true);
				btnStop.setEnabled(true);
			}
			
			frame.setSize(frame.getWidth(), frame.getWidth());
			
			animContrl = new AnimationController(instance);
			animPlaying = false;
		}
		
		@Override
		public void resize(int width, int height)
		{
			viewMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); //update matrix for 2d
		}
		
		/**
		 * Process all waiting events
		 */
		public void processEvents()
		{
			Iterator<Event> itr = eventList.iterator();
			while (itr.hasNext())
			{
				Event e = itr.next();
				
				if (e.eventType == EventType.CHANGEFILE)
				{
					assets.load(e.eventInfo, Model.class);
					loading = true;
					currentPath = e.eventInfo;
					itr.remove();
				}
				
				if (e.eventType == EventType.PLAYANIM)
				{
					animPlaying = true;
					//using selected animation (from spinner), with loop count equal to Integer.MAX_VALUE
					animContrl.animate(instance.animations.get((int) animSpinner.getValue()).id, Integer.MAX_VALUE, 1, null, 0);
					itr.remove();
					
				}
				
				if (e.eventType == EventType.STOPANIM)
				{
					//don't know how to stop animation, just set loop count to 1 and animation will be stoped automaticly when it's finished 
					animContrl.current.loopCount = 1;
					
					itr.remove();
				}
				
				if (e.eventType == EventType.SCALE)
				{
					float scale = Float.parseFloat(e.eventInfo);
					
					//resource: http://www.physics.nyu.edu/grierlab/idl_html_help/obj_transform8.html
					float[] val = { scale, 0, 0, 0,
							0, scale, 0, 0, 
							0, 0, scale, 0, 
							0, 0, 0, 1 };
					Matrix4 scaleMatrix = new Matrix4().set(val);
					
					instance.transform.mul(scaleMatrix);
					itr.remove();
				}
			}
			
		}
		
		@Override
		public void render()
		{
			//event is waiting
			if (eventList.size() > 0)
				processEvents(); //process it
			
			//loading models if finished
			if (loading && assets.update())
				doneLoading();

			if (animContrl != null && animPlaying)
			{
				if (animContrl.current.loopCount == 0)
					animPlaying = false;
				
				animContrl.update(Gdx.graphics.getDeltaTime());
			}
			
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			cam.update();
			
			if (optRenderText)
			{
				batch.setProjectionMatrix(viewMatrix);
				batch.begin();
				font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 5, Gdx.graphics.getHeight() - 5);
				if (instance != null)
				{
					font.draw(batch, "Materials: " + instance.materials.size, 5, Gdx.graphics.getHeight() - 20);
					font.draw(batch, "Animations: " + instance.animations.size, 5, Gdx.graphics.getHeight() - 35);
				}
				batch.end();	
			}
			
			camController.update();
			
			modelBatch.begin(cam);
			
			if (instance != null)
			{
				if (optRenderLight)
					modelBatch.render(instance, lights);
				else
					modelBatch.render(instance);
			}
			
			if (optRenderLines)
			{
				modelBatch.render(xi);
				modelBatch.render(yi);
				modelBatch.render(zi);
			}
			
			modelBatch.end();
		}
		
		@Override
		public void pause()
		{

		}
		
		@Override
		public void resume()
		{
			
			
		}
		
		@Override
		public void dispose()
		{
			//this create exception "No OpenGL context found in the current thread." don't now why and how to fix it.
//			x.dispose();
//			y.dispose();
//			z.dispose();
//			modelBatch.dispose();
		}
		
	}
	
}