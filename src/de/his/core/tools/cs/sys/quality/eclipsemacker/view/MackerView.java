package de.his.core.tools.cs.sys.quality.eclipsemacker.view;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.his.core.tools.cs.sys.quality.eclipsemacker.builder.MackerBuilder;

/**
 * Dient zur Debug-Ausgabe
 * @author Bender
 */
public class MackerView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "com.myco.viewplugin.views.SampleView";

    TableViewer viewer;

    private Action action1;

    ArrayList<String> all = MackerBuilder.builderErrors;

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

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            // empty
        }

        @Override
        public void dispose() {
            // empty
        }

        @Override
        public Object[] getElements(Object parent) {

            if (all.size() > 0) {
                Object test[] = new Object[all.size()];

                for (int i = 0; i < all.size(); i++) {
                    test[i] = all.get(i);
                }
                return test;
            }

            return new Object[] { new String("Error") };
        }
    }


    /**
     * @return the all
     */
    public ArrayList<String> getAll() {
        return all;
    }

    /**
     * @param all the all to set
     */
    public void setAll(ArrayList<String> all) {
        this.all = all;
    }






    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public String getColumnText(Object obj, int index) {

            if (all.size() > 0) {

                String instr = (String) obj;

                switch (index) {
                    case 0:
                        return instr;
                    case 1:
                        return new Integer(1).toString();

                }
            }

            return getText(obj);
        }

        @Override
        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }

        @Override
        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
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
    @Override
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
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new ViewerSorter());
        viewer.setInput(getViewSite());

        makeActions();
        hookContextMenu();
        contributeToActionBars();
    }


    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
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

    void fillContextMenu(IMenuManager manager) {
        manager.add(action1);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(action1);
    }

    private void makeActions() {
        action1 = new Action() {
            @Override
            public void run() {
                viewer.refresh();
            }
        };

        action1.setText("Debug");
        action1.setToolTipText("Show Debug");
        action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));


    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}
