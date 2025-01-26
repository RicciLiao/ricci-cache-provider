package ricciliao.cache.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

public class HeaderRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> extraHeader;

    public HeaderRequestWrapper(HttpServletRequest request, Map<String, String> extraHeader) {
        super(request);
        this.extraHeader = extraHeader;
    }

    @Override
    public String getHeader(String name) {
        if (extraHeader.containsKey(name)) {
            return extraHeader.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Enumeration<String> originalHeaders = super.getHeaderNames();
        Vector<String> allHeaders = new Vector<>();

        while (originalHeaders.hasMoreElements()) {
            allHeaders.add(originalHeaders.nextElement());
        }
        allHeaders.addAll(extraHeader.keySet());

        return allHeaders.elements();
    }

}
