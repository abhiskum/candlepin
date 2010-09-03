/**
 * Copyright (c) 2009 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.fedoraproject.candlepin.pinsetter.tasks;

import java.util.List;

import org.fedoraproject.candlepin.controller.PoolManager;
import org.fedoraproject.candlepin.model.Pool;
import org.fedoraproject.candlepin.model.PoolCurator;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Inject;

/**
 * The Class RegenEntitlementCertsJob.
 */
public class RegenEntitlementCertsJob implements Job {
    
    private PoolCurator poolCurator;
    private PoolManager poolManager;
    public static final String PROD_ID = "product_id";
    @Inject
    public RegenEntitlementCertsJob(PoolCurator poolCurator, PoolManager poolManager) {
        this.poolCurator = poolCurator;
        this.poolManager = poolManager;
    }
    
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        String prodId = arg0.getJobDetail().getJobDataMap().getString(
            "product_id");
        List<Pool> poolsForProduct = poolCurator.listAvailableEntitlementPools(
            null, null, prodId, false);
        for (Pool pool : poolsForProduct) {
            poolManager.regenerateCertificatesOf(pool.getEntitlements());
        }
    }
}
