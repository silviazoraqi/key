package org.key_project.key4eclipse.starter.ui.handler;

import java.io.FileNotFoundException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.key_project.key4eclipse.util.key.KeYUtil;

import de.uka.ilkd.key.proof.init.ProofInputException;

/**
 * Handler that starts the KeY UI via {@link KeYUtil#openMainWindow()}.
 */
@SuppressWarnings("restriction")
public class StartProofHandler extends AbstractSaveExecutionHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doExecute(ExecutionEvent event) throws FileNotFoundException, JavaModelException, ProofInputException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object[] elements = ((IStructuredSelection)selection).toArray();
            for (Object element : elements) {
                if (element instanceof IMethod) {
                    KeYUtil.startProof((IMethod)element);
                }
            }
        }
        else if (selection instanceof ITextSelection) {
            ITextSelection textSelection = (ITextSelection)selection;
            IEditorPart editor = HandlerUtil.getActiveEditor(event);
            if (editor instanceof JavaEditor) {
                JavaEditor javaEditor = (JavaEditor)editor;
                IJavaElement element = SelectionConverter.resolveEnclosingElement(javaEditor, textSelection);
                if (element instanceof IMethod) {
                    KeYUtil.startProof((IMethod)element);
                }
            }
        }
        return null;
    }
}