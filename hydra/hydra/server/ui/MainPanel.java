/*
 * MainToolPanel.java
 *
 * Created on October 2, 2001, 11:20 AM
 */

package org.hydra.server.ui;
import javax.swing.* ;
import javax.swing.event.* ;
import org.hydra.server.* ;
import java.lang.reflect.* ;
import javax.swing.tree.* ;
import java.util.* ;
import java.awt.* ;

/**
 *  Creates a split-pane panel with a left tree component full of tools and a right
 *  panel.
 *
 * @author  wpeng
 * @version
 */
public class MainPanel extends javax.swing.JPanel {
    protected static final String ROOT = "ROOT" ;
    
    static class ClassComponentTuple {
        ClassComponentTuple( Class clazz, Component component, String name ) {
            this.clazz = clazz ;
            this.component = component ;
            this.name = name ;
        }
        Class clazz ;
        Component component ;
        String name ;
        
        public String toString() {
            if ( name != null ) return name ;
            return "" + clazz.toString() ;
        }
    }
    
    class PanelTreeModel implements TreeModel {
        
        public java.lang.Object getRoot() {
            return ROOT ;
        }
        
        public void update() {
            for (int i=0;i<list.size();i++) {
                TreeModelListener tml = ( TreeModelListener ) list.get(i) ;
                tml.treeStructureChanged( new TreeModelEvent( this, new TreePath( ROOT ) ) ); 
            }
        }
        
        public int getIndexOfChild(java.lang.Object obj, java.lang.Object obj1) {
            if ( obj == ROOT ) {
                return categories.indexOf( obj1 ) ;
            }
            else if ( obj instanceof ArrayList ) {
                ArrayList al = ( ArrayList ) obj ;
                return al.indexOf( obj1 ) - 1 ;
            }
            return -1 ;
        }
        
        public boolean isLeaf(java.lang.Object obj) {
            if ( obj == ROOT || obj instanceof ArrayList ) {
                return false  ;
            }
            return true ;
        }
        
        public java.lang.Object getChild(java.lang.Object obj, int param) {
            if ( obj == ROOT ) {
                return categories.get( param ) ;
            }
            else if ( obj instanceof ArrayList ) {
                ArrayList al = ( ArrayList ) obj ;
                return al.get( param + 1 ) ;
            }
            return null ;
        }
        
        public void valueForPathChanged(javax.swing.tree.TreePath treePath, java.lang.Object obj) {
        }
        
        public int getChildCount(java.lang.Object obj) {
            if ( obj == ROOT ) {
                return categories.size() ;
            }
            else if ( obj instanceof ArrayList ) {
                ArrayList al = ( ArrayList ) obj ;
                return al.size() - 1 ;
            }
            return 0 ;
        }
        
        public void addTreeModelListener(javax.swing.event.TreeModelListener treeModelListener) {
            list.add( treeModelListener ) ;
        }
        
        public void removeTreeModelListener(javax.swing.event.TreeModelListener treeModelListener) {
            list.remove( treeModelListener ) ;
        }
        
        ArrayList list = new ArrayList() ;
    }
    
    /** Creates new MainToolPanel */
    public MainPanel( ServerApp app ) {
        this.app = app ;
        buildLayout() ;        
        makePanels() ;
        // panelTree.setSelectionPath() ;
    }

    public void refreshVisible() {
        Component c = sp.getRightComponent() ;
        if ( c instanceof AppComponent ) {
            ( ( AppComponent ) c ).update() ;
        }
    }
    
    protected void buildLayout() {
        setLayout( new BorderLayout() ) ;
        sp = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ; 
        sp.setDividerLocation( 250 ) ;
        add( sp, BorderLayout.CENTER ) ;
        panelTree = new JTree( panelTreeModel = new PanelTreeModel() ) ;
        panelTree.setShowsRootHandles( false ) ;
        panelTree.setRootVisible( false ) ;
        TreeSelectionModel tsm ;
        panelTree.setSelectionModel( tsm = new DefaultTreeSelectionModel() ) ;
        tsm.setSelectionMode( DefaultTreeSelectionModel.SINGLE_TREE_SELECTION ) ;
        sp.setLeftComponent( panelTree ) ;
        sp.setRightComponent( emptyPanel ) ;
        panelTree.addTreeSelectionListener( new TreeSelectionListener() {
            public void valueChanged( TreeSelectionEvent e ) {
                doTreeSelectionChanged() ;
            }
        } ) ;
    }
    
    protected void doTreeSelectionChanged() {
        TreePath path = panelTree.getSelectionPath() ;
        
        if ( path == null ) {
           int loc = sp.getDividerLocation() ;
           sp.setRightComponent( emptyPanel ) ;
           sp.setDividerLocation( loc ) ;
           return ;            
        }
        Object o = path.getLastPathComponent() ;
        int loc = sp.getDividerLocation() ;
        if ( o instanceof ClassComponentTuple ) {
           ClassComponentTuple cct = ( ClassComponentTuple ) o ;
           if ( cct.component != null ) {
               sp.setRightComponent( cct.component ) ;
           }
           else {
               Class p = cct.clazz ;
               try {
                Component c = ( Component ) p.newInstance() ;
                cct.component = c ;
                ( ( AppComponent ) c ).setApp( app ) ;
                sp.setRightComponent( c ) ;
               }
               catch ( Exception e ) {
                    e.printStackTrace() ;
               }
           }
           sp.setDividerLocation( loc ) ;
           refreshVisible() ;
        }
    }
    
    protected void makePanels() {
        addPanel( AgentTablePanel.class ) ;
        addPanel( LogPanel.class ) ;
        panelTreeModel.update() ;
    }
    
    public void addCategory( String s ) {
        ArrayList cat = new ArrayList() ;
        cat.add( s ) ;
        categories.add( cat ) ;
    }
    
    public void addPanel( Class panelClass ) {
        if ( ! AppComponent.class.isAssignableFrom(panelClass ) || !Component.class.isAssignableFrom(panelClass) ) {
            throw new RuntimeException( panelClass + " must implement AppComponent and java.awt.Component interface." ) ;
        }

        String name = panelClass.getName() ;        
        try {
            Method m = panelClass.getMethod( "getAppComponentName", new Class[0] ) ;
            if ( ( m.getModifiers() & Modifier.STATIC ) != 0 && m.getReturnType() == String.class ) {
                name = ( String ) m.invoke( null, null ) ;
            }
        }
        catch ( Exception e ) {
            e.printStackTrace() ;
        }

        ClassComponentTuple cct = new ClassComponentTuple( panelClass, null, name ) ;
        categories.add( cct )  ;
    }
    
    public void addPanel( String category, Class panelClass ) {
        if ( ! AppComponent.class.isAssignableFrom(panelClass ) || !Component.class.isAssignableFrom(panelClass)) {
            throw new RuntimeException( panelClass + " must implement AppComponent interface." ) ;
        }
        
        String name = panelClass.getName() ;        
        try {
            Method m = panelClass.getMethod( "getAppComponentName", new Class[0] ) ;
            if ( ( m.getModifiers() & Modifier.STATIC ) != 0 && m.getReturnType() == String.class ) {
                name = ( String ) m.invoke( null, null ) ;
            }
        }
        catch ( Exception e ) {}
        
        for (int i=0;i<categories.size();i++) {
            ArrayList al = ( ArrayList ) categories.get(i) ;
            if ( al.get(0).equals( category ) ) {
                ClassComponentTuple cct = new ClassComponentTuple( panelClass, null, name ) ;
            }
        }
    }
    
    JPanel emptyPanel = new JPanel() ;
    PanelTreeModel panelTreeModel ;
    ServerApp app ;
    JSplitPane sp ;
    JTree panelTree ;
    ArrayList categories = new ArrayList() ;
}
