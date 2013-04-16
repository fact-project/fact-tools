package fact.viewer.ui;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;




/**
 * This class implements a simple progress dialog. It is initialized with a task and
 * implements the <code>TaskMonitor</code> interface. Thus, it simply registers as a
 * monitor is notified about all advancements of the task.
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class TaskProgressDialog
    extends Dialog
    implements TaskMonitor
{

    private static final long serialVersionUID = 3166807058880173136L;
    
    /** A label for displaying the task status message */
    JLabel msg = new JLabel();
    
    /** A label for displaying the icon */
    JLabel icon = new JLabel();
    
    /** The bar to display the task's progress */
    JProgressBar progress = new JProgressBar(0, 100);
    
    /** The task that is monitored by this dialog */
    Task task = null;
    
    
    /**
     * This creates a new task progress dialog which displays the progress of the
     * given task <code>t</code>.
     * 
     * @param parent The parent component of this dialog.
     * @param t The task which's progress is to be displayed.
     */
    public TaskProgressDialog( JFrame parent, Task t ){
    	super(parent);
    	task = t;
    	task.addTaskMonitor( this );
    	setModal( true );
    }

    
    
    /**
     * This creates a new instance of this class without an initial task. This can be
     * used to later on externally register this dialog to a task. 
     * 
     * @param parent The parent component of this dialog.
     * @param iconName The name of an icon that is to be displayed.
     */
    public TaskProgressDialog( JFrame parent, ImageIcon iconName ){
    	super( parent );
    	setResizable( false );
    	setUndecorated( false );
        
        icon.setBounds( 17,6,32,32 );
        icon.setIcon( iconName );
        add( icon );
        
        msg.setBounds( 65, 12, 220, 22 );
        add( msg );
        
        //b.setVisible(true);
        progress.setValue(0);
        //progress.setBorder( null );
        progress.setStringPainted( true );
        progress.setForeground( Color.ORANGE );
        progress.setBounds( 12, 48, 230, 16 );

        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel("Reading events "));
        
        getContentPane().setLayout( null );
        getContentPane().add(progress); 

        setSize( 260, 108 );
        
        setAlwaysOnTop(true);
        center();
        setModal(true);
    }

    
    /**
     * This method sets the progress of the bar to the given value.
     * 
     * @param val The new value of the progress bar.
     * @deprecated
     */
    public void setProgress( int val ){
    	progress.setValue( val );
    	progress.repaint();
    }

    
    /**
     * @see org.jwall.ui.TaskMonitor#taskStarted(org.jwall.Task)
     */
    public void taskStarted( Task t ){
    	setVisible( true );
    }

    
    /**
     * @see org.jwall.ui.TaskMonitor#taskAdvanced(org.jwall.Task)
     */
	public void taskAdvanced( Task t ) {
		progress.setValue( ( new Double( t.percentageCompleted() ) ).intValue() );
		msg.setText( t.getStatus() );
		progress.validate();
	}

	
	/**
	 * @see org.jwall.ui.TaskMonitor#taskFinished(org.jwall.Task)
	 */
	public void taskFinished( Task t ) {
		setVisible( false );
	}

	
	/**
	 * @see org.jwall.ui.TaskMonitor#taskPaused(org.jwall.Task)
	 */
	public void taskPaused( Task t ) {
	}

	
	/**
	 * @see org.jwall.ui.TaskMonitor#taskResumed(org.jwall.Task)
	 */
	public void taskResumed( Task t ) {
	}
	
	
	/**
	 * This method sets the icon to be displayed next to the task's status message.
	 * 
	 * @param i The icon to be displayed.
	 */
	public void setIcon( ImageIcon i ){
		icon.setIcon( i );
	}
}