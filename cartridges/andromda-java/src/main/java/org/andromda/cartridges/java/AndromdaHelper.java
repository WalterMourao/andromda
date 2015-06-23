package org.andromda.cartridges.java;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.uml2.uml.Comment;

/**
 * Helper class for JDT/Andromda cartridges.
 * 
 * @author Walter Mourão
 * @since 1.0.0
 * @version 1.0.0
 */
public class AndromdaHelper {
  //TODO some of the code here should go to the KissMDA JdtHelper.
    
    @SuppressWarnings("unchecked")
    public BodyDeclaration createComment(AST ast, BodyDeclaration bodyDeclaration, String comment){
        final Javadoc javadoc= ast.newJavadoc();
        final TextElement textElement=ast.newTextElement();
        textElement.setText(comment);
        final TagElement tagElement=ast.newTagElement();
        tagElement.fragments().add(textElement);
        javadoc.tags().add(tagElement);
        bodyDeclaration.setJavadoc(javadoc);
        return bodyDeclaration;
    }

    public String concatComments(EList<Comment> comments){
        if(comments.isEmpty()){
            return StringUtils.EMPTY;
        } else {
            StringBuilder sb=new StringBuilder();
            for(Comment comment: comments){
                sb.append(comment.getBody()).append('\n');
            }
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        }
    }
}
