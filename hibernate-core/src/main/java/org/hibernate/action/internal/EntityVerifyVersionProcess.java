/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.action.internal;

import org.hibernate.OptimisticLockException;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;

/**
 * A BeforeTransactionCompletionProcess impl to verify an entity version as part of
 * before-transaction-completion processing
 *
 * @author Scott Marlow
 */
public class EntityVerifyVersionProcess implements BeforeTransactionCompletionProcess {
	private final Object object;
	private final EntityEntry entry;

	/**
	 * Constructs an EntityVerifyVersionProcess
	 *
	 * @param object The entity instance
	 * @param entry The entity's referenced EntityEntry
	 */
	public EntityVerifyVersionProcess(Object object, EntityEntry entry) {
		this.object = object;
		this.entry = entry;
	}

	@Override
	public void doBeforeTransactionCompletion(SessionImplementor session) {
		final EntityPersister persister = entry.getPersister();

		if ( !entry.isExistsInDatabase() ) {
			// HHH-9419: We cannot check for a version of an entry we ourselves deleted
			return;
		}

		final Object latestVersion = persister.getCurrentVersion( entry.getId(), session );
		if ( !entry.getVersion().equals( latestVersion ) ) {
			throw new OptimisticLockException(
					object,
					"Newer version [" + latestVersion +
							"] of entity [" + MessageHelper.infoString( entry.getEntityName(), entry.getId() ) +
							"] found in database"
			);
		}
	}
}
