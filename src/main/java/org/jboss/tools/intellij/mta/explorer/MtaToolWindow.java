package org.jboss.tools.intellij.mta.explorer;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.project.Project;
import com.intellij.ui.*;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.mta.cli.MtaCliRunner;
import org.jboss.tools.intellij.mta.explorer.nodes.*;
import org.jboss.tools.intellij.mta.model.MtaModel;
import org.jboss.tools.intellij.mta.services.ModelService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;

public class MtaToolWindow extends SimpleToolWindowPanel {

    private ModelService modelService;
    private MtaCliRunner cliRunner;
    private Project project;

    public MtaToolWindow(ModelService modelService, Project project) {
        super(true, true);
        this.modelService = modelService;
        this.project = project;
        this.init();
        this.cliRunner = new MtaCliRunner();
    }

    private void init() {
        MtaExplorerTreeStructure treeStructure = new MtaExplorerTreeStructure(modelService);
        StructureTreeModel structureTreeModel = new StructureTreeModel(treeStructure, modelService);
        AsyncTreeModel asyncTreeModelModel = new AsyncTreeModel(structureTreeModel, true, project);
        Tree mtaTree = this.createTree(asyncTreeModelModel, this.modelService.getModel(), structureTreeModel);
        JScrollPane mtaTreePanel = ScrollPaneFactory.createScrollPane(mtaTree);
        NonOpaquePanel treePanelWrapper = new NonOpaquePanel();
        treePanelWrapper.setContent(mtaTreePanel);
        PopupHandler.installPopupHandler(mtaTree, "org.jboss.tools.intellij.mta.explorer", ActionPlaces.UNKNOWN);
        super.setContent(treePanelWrapper);
    }

    private Tree createTree(AsyncTreeModel asyncTreeModel, MtaModel model, StructureTreeModel treeModel) {
        Tree mtaTree = new Tree(asyncTreeModel);
        TreeUIHelper.getInstance().installTreeSpeedSearch(mtaTree);
        mtaTree.setRootVisible(false);
        mtaTree.setAutoscrolls(true);
        mtaTree.setCellRenderer(new MtaTreeCellRenderer(modelService, treeModel));
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent event) {
                TreePath path = mtaTree.getClosestPathForLocation(event.getX(), event.getY());
                if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (treeNode.getUserObject() instanceof MtaExplorerNode) {
                        MtaExplorerNode mtaNode = (MtaExplorerNode) treeNode.getUserObject();
                        mtaNode.onDoubleClick(MtaToolWindow.this.project, treeModel);
                    }
                }
                return true;
            }
        }.installOn(mtaTree);
        new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                TreePath path = mtaTree.getClosestPathForLocation(event.getX(), event.getY());
                if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (treeNode.getUserObject() instanceof MtaExplorerNode) {
                        MtaExplorerNode mtaNode = (MtaExplorerNode) treeNode.getUserObject();
                        if (mtaNode instanceof IssueNode || mtaNode instanceof ReportNode) {
                            mtaNode.onClick(MtaToolWindow.this.project);
                        }
                    }
                }
                return true;
            }
        }.installOn(mtaTree);
        return mtaTree;
    }
}