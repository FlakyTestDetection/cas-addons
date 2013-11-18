package net.unicon.cas.addons.serviceregistry.mongodb;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of <code>ServiceRegistryDao</code> that uses a MongoDb repository as the backend persistence mechanism.
 * The repository is configured by the Spring application context.
 * <p/>
 * <p>The class will automatically create a default collection to use with services. The name of the collection may be specified
 * through {@link #setCollectionName(String)}. It also presents the ability to drop an existing collection and start afresh
 * through the use of {@link #setDropCollection(boolean)}.</p>
 *
 * @author <a href="mailto:mmoayyed@unicon.net">Misagh Moayyed</a>
 * @author Unicon, inc.
 * @since 1.0.1
 */
@Repository
public final class MongoServiceRegistryDao implements ServiceRegistryDao, InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(MongoServiceRegistryDao.class);

	private static final String MONGODB_COLLECTION_NAME = RegisteredService.class.getSimpleName();

	private String collectionName = MONGODB_COLLECTION_NAME;

	private boolean dropCollection = false;

	@Autowired
	@NotNull
	private final MongoOperations mongoTemplate = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.dropCollection) {
			log.debug("Dropping database collection: {}", this.collectionName);
			this.mongoTemplate.dropCollection(this.collectionName);
		}

		if (!this.mongoTemplate.collectionExists(this.collectionName)) {
			log.debug("Creating database collection: {}", this.collectionName);
			this.mongoTemplate.createCollection(this.collectionName);
		}
	}

	@Override
	@Transactional(readOnly = false)
	public boolean delete(final RegisteredService svc) {
		if (this.findServiceById(svc.getId()) != null) {
			this.mongoTemplate.remove(svc, this.collectionName);
			log.debug("Removed registered service: {}", svc);
			return true;
		}
		return false;
	}

	@Override
	@Transactional(readOnly = true)
	public RegisteredService findServiceById(final long svcId) {
		return this.mongoTemplate.findOne(new Query(Criteria.where("id").is(svcId)), RegisteredService.class, this.collectionName);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RegisteredService> load() {
		return this.mongoTemplate.findAll(RegisteredService.class, this.collectionName);
	}

	@Override
	@Transactional(readOnly = false)
	public RegisteredService save(final RegisteredService svc) {
    if (svc.getId() == -1) {
        ((AbstractRegisteredService) svc).setId(svc.hashCode());
    }
		this.mongoTemplate.save(svc, this.collectionName);
		log.debug("Saved registered service: {}", svc);
		return this.findServiceById(svc.getId());
	}

	/**
	 * Optionally, specify the name of the mongodb collection where services are to be kept.
	 * By default, the name of the collection is specified by the constant {@link #MONGODB_COLLECTION_NAME}
	 */
	public void setCollectionName(final String name) {
		this.collectionName = name;
	}

	/**
	 * When set to true, the collection will be dropped first before proceeding with other operations.
	 */
	public void setDropCollection(final boolean dropCollection) {
		this.dropCollection = dropCollection;
	}
}
