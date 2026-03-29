package com.ess.mongoexp;
import org.codehaus.janino.ExpressionEvaluator;
import org.slf4j.Logger;

public class expressionEvaluator {
    public static String evaluateExpression(Logger logger, String expressionString) {
        try{
            ExpressionEvaluator ee = new ExpressionEvaluator();
            //ee.setExpressionType(Date.class);
            //ee.setParameters(new String[]{}, new Class[]{});
            ee.cook(expressionString); // compiles the expression
            return (ee.evaluate(new Object[]{})).toString();
        }catch (Exception e) {
            logger.error("Exiting Method : ConfigLoader.load 2 : " + e.getStackTrace().toString());
            logger.error(e.getMessage());
            return null;
        }
    }
}
