package net.unicon.cas.addons.response;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.web.view.AbstractCasView;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * An alternative lightweight CAS validation response view representing a service ticket validation success
 * that marshals an authenticated principal's attributes as a JSON String.
 *
 * @author Dmitriy Kopylenko
 * @author Unicon, inc.
 * @since 0.6
 */
public class ServiceValidateSuccessJsonView extends AbstractCasView {

    /**
     * Once the instance is constructed, it is thread-safe
     */
    private final ObjectMapper jacksonObjectMapper = new ObjectMapper();

    @Override
    protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Authentication authentication = getAssertionFrom(model).getChainedAuthentications().get(0);
        final Principal principal = authentication.getPrincipal();
        this.jacksonObjectMapper.writeValue(response.getWriter(), new TicketValidationJsonResponse(authentication, principal));
    }
}
