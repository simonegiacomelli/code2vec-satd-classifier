package satd.step2.perf

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class PredictionFileKtTest {

    @Test
    fun testExtractPrediction_correct() {
        val dir = createTempDir()
        val file = File(dir, "000005_000025_39892DA4B1B0817AA877BFB816F94BFE4D89F471_satd.java")
        file.writeText(correct)
        val res = extractPrediction(file)
        val s = res.sample

        assertEquals(5, s.index)
        assertEquals(25, s.satdId)
        assertEquals("satd", s.type)

        assertEquals("satd", res.prediction)
        assertEquals(0.741230, res.confidence)

    }

    @Test
    fun testExtractPrediction_wrong() {
        val dir = createTempDir()
        val file = File(dir, "000010_000109_8B392F29C2DE857A3D4C49B63074AC84B2CD0042_fixed.java")
        file.writeText(wrong)
        val res = extractPrediction(file)
        val s = res.sample

        assertEquals(10, s.index)
        assertEquals(109, s.satdId)
        assertEquals("fixed", s.type)

        assertEquals("satd", res.prediction)
        assertEquals(0.974789, res.confidence)

    }
}

private val correct =
    """/*
Prediction:	satd
Actual:	satd
	(0.741230) predicted: ['satd']
	(0.258113) predicted: ['fixed']
Attention:
0.038010	context: virtualfile,(ClassOrInterfaceType0)^(VariableDeclarationExpr)^(ForeachStmt)^(BlockStmt)_(IfStmt)_(ExpressionStmt)_(AssignExpr:assign0)_(NameExpr0),mylastdir
0.030027	context: virtualfile,(ClassOrInterfaceType2)^(Parameter)^(MethodDeclaration)_(BlockStmt)_(ForeachStmt)_(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.024155	context: vcs,(VariableDeclaratorId1)^(Parameter)^(MethodDeclaration)_(BlockStmt)_(ForeachStmt)_(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.024155	context: vcs,(ClassOrInterfaceType2)^(Parameter)^(MethodDeclaration)_(BlockStmt)_(ForeachStmt)_(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.015859	context: vfiles,(VariableDeclaratorId1)^(Parameter)^(MethodDeclaration)_(BlockStmt)_(ForeachStmt)_(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.015098	context: virtualfile,(ClassOrInterfaceType2)^(Parameter)^(MethodDeclaration)_(BlockStmt)_(IfStmt)_(BinaryExpr:and)_(BinaryExpr:equals)_(FieldAccessExpr0)_(NameExpr2),length
0.015098	context: virtualfile,(ClassOrInterfaceType0)^(VariableDeclarationExpr)^(ForeachStmt)_(IfStmt)_(BinaryExpr:and)_(BinaryExpr:greater)_(FieldAccessExpr0)_(NameExpr2),length
0.015098	context: virtualfile,(ClassOrInterfaceType0)^(VariableDeclarationExpr)^(ForeachStmt)^(BlockStmt)_(IfStmt)_(BinaryExpr:and)_(BinaryExpr:equals)_(FieldAccessExpr0)_(NameExpr2),length
0.014700	context: virtualfile,(ClassOrInterfaceType0)^(VariableDeclarationExpr)^(ForeachStmt)_(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.014042	context: virtualfile,(ClassOrInterfaceType2)^(Parameter)^(MethodDeclaration)_(BlockStmt)_(IfStmt)_(BinaryExpr:and)_(MethodCallExpr1)_(ArrayAccessExpr0)_(IntegerLiteralExpr1),0
*/

public class Wrapper{
    @Override
    protected boolean satd(@NotNull Project project, @NotNull Vcs vcs, @NotNull VirtualFile... vFiles) {
        for (VirtualFile file : vFiles) if (file.isDirectory() && vFiles.length > 1)
            return false;
        if (vFiles.length == 1 && vFiles[0].isDirectory())
            myLastDir = vFiles[0];
        else
            myLastDir = null;
        return true;
    }
}    
""".trimIndent()

private val wrong = """/*
Prediction:	satd
Actual:	fixed
	(0.974789) predicted: ['satd']
	(0.025103) predicted: ['fixed']
Attention:
0.307185	context: getuser,(NameExpr3)^(MethodCallExpr)^(VariableDeclarator)^(VariableDeclarationExpr)^(ExpressionStmt)^(BlockStmt)_(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.095145	context: null,(NullLiteralExpr0)^(BinaryExpr:equals)^(IfStmt)^(BlockStmt)_(ReturnStmt)_(MethodCallExpr0)_(NameExpr2),role
0.062373	context: user,(NameExpr1)^(BinaryExpr:equals)^(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.060963	context: null,(NullLiteralExpr0)^(BinaryExpr:equals)^(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.036121	context: null,(NullLiteralExpr0)^(BinaryExpr:equals)^(IfStmt)^(BlockStmt)_(ReturnStmt)_(MethodCallExpr0)_(NameExpr3),hasrole
0.033786	context: false,(BooleanLiteralExpr0)^(ReturnStmt)^(IfStmt)^(BlockStmt)_(ExpressionStmt)_(VariableDeclarationExpr)_(VariableDeclarator)_(MethodCallExpr1)_(NameExpr0),museradmin
0.030744	context: user,(NameExpr1)^(BinaryExpr:equals)^(IfStmt)^(BlockStmt)_(ReturnStmt)_(MethodCallExpr0)_(NameExpr3),hasrole
0.022207	context: paxwicketauth,(NameExpr0)^(MethodCallExpr)^(VariableDeclarator)^(VariableDeclarationExpr)^(ExpressionStmt)^(BlockStmt)_(IfStmt)_(ReturnStmt)_(BooleanLiteralExpr0),false
0.018718	context: getuser,(NameExpr3)^(MethodCallExpr)^(VariableDeclarator)^(VariableDeclarationExpr)^(ExpressionStmt)^(BlockStmt)_(IfStmt)_(BinaryExpr:equals)_(NameExpr1),user
0.017923	context: null,(NullLiteralExpr0)^(BinaryExpr:equals)^(IfStmt)^(BlockStmt)_(ExpressionStmt)_(VariableDeclarationExpr)_(VariableDeclarator)_(MethodCallExpr1)_(NameExpr3),getauthorization
*/

public class Wrapper{
    private boolean fixed(String role) {
        final PaxWicketAuthentication paxWicketAuth = (PaxWicketAuthentication) AuthenticatedWebSession.get();
        final String loginName = paxWicketAuth.getLoggedInUser();
        final User user = getUser(m_userAdmin, loginName);
        if (null == user)
            return false;
        final Authorization auth = m_userAdmin.getAuthorization(user);
        return auth.hasRole(role);
    }
}"""