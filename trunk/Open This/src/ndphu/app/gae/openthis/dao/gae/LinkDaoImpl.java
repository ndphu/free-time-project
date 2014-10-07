package ndphu.app.gae.openthis.dao.gae;

import ndphu.app.gae.openthis.dao.LinkDao;
import ndphu.app.gae.openthis.model.Link;

public class LinkDaoImpl extends AbstractDao<Link> implements LinkDao {

	public LinkDaoImpl() {
		super(Link.class);
	}
}
