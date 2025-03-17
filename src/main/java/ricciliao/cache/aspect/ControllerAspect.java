package ricciliao.cache.aspect;

import org.aopalliance.intercept.MethodInvocation;
import ricciliao.x.aop.DynamicAspect;
import ricciliao.x.component.exception.CmnParameterException;
import ricciliao.x.component.response.ResponseUtils;

public class ControllerAspect extends DynamicAspect {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {

            return super.invoke(invocation);
        } catch (CmnParameterException e) {

            return ResponseUtils.builder().code(e.getCode()).build();
        } catch (Exception e) {

            return ResponseUtils.errorResponse();
        }
    }

}
