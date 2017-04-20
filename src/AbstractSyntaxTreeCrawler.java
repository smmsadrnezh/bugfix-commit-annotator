import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by smmsadrnezh on 2/20/17.
 */


public class AbstractSyntaxTreeCrawler {
    public void buildAST(String code , Integer lineNumber) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {

            Set names = new HashSet();

            public boolean visit(VariableDeclarationFragment node) {
                SimpleName name = node.getName();
//                if (cu.getLineNumber(name.getStartPosition()) == lineNumber) {
                    this.names.add(name.getIdentifier());
                    System.out.println(name);
//                }
                return false;
            }

            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
//                    if (cu.getLineNumber(node.getStartPosition()) == lineNumber) {
                        System.out.println(node);
//                    }
                }
                return true;
            }

        });
    }
}
