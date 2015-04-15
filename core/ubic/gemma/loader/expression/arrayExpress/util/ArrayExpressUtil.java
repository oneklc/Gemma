package ubic.gemma.loader.expression.arrayExpress.util;

import ubic.gemma.util.ConfigUtils;
import ubic.gemma.util.NetDatasourceUtil;

/**
 * @author pavlidis
 * @version $Id: ArrayExpressUtil.java,v 1.6 2006/09/28 23:16:24 paul Exp $
 */
public class ArrayExpressUtil extends NetDatasourceUtil {

    @Override
    public void init() {
        this.setHost( ConfigUtils.getString( "arrayExpress.host" ) );
        assert this.getHost() != null;
    }

}
