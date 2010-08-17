/**
 * 
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.view;

import java.util.ArrayList;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;

import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker.CustomMacker;

/**
 * @author Bender
 *
 */
public class MackerView  extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.myco.viewplugin.views.SampleView";

	private TableViewer viewer;
	private Action action1;
	private ArrayList<CustomMacker> all = CustomMacker.all;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {

			if (all.size() > 0) {
				Object test[] = new Object[all.size()];
				
				for (int i = 0; i < all.size(); i++) {
					test[i] = (Object)all.get(i);
				}
				return test;
			}
			
			return new Object[] {new String("Error")};
		}
	}
	
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {


		public String getColumnText(Object obj, int index) {
			
			if (all.size() > 0) {

			CustomMacker instr = (CustomMacker) obj;
			
			switch (index) {
			case 0:
				return new String("");
			case 1:
				return new Integer(1).toString();
			
			}
			}
			
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	class NameSorter extends ViewerSorter {
	}

	
	/**
	 * The constructor.
	 */
	public MackerView() {
		
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		Table table = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		TableLayout layout = new TableLayout();
		
		table.setLayout(layout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableColumn col1 = new TableColumn(table, SWT.None);
		col1.setText("Class File Found");
		col1.setWidth(150);
		TableColumn col2 = new TableColumn(table, SWT.None);
		col2.setText("Events Found");
		col2.setWidth(150);

		
		viewer = new TableViewer(table);
		System.out.println("neu");
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MackerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				viewer.refresh();
			}
		};
		
		action1.setText("Debug");
		action1.setToolTipText("Show Debug");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		

		}
		
	


	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}