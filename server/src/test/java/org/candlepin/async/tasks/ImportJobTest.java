/**
 * Copyright (c) 2009 - 2019 Red Hat, Inc.
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
package org.candlepin.async.tasks;

import org.candlepin.async.JobArguments;
import org.candlepin.async.JobConfig;
import org.candlepin.async.JobConfigValidationException;
import org.candlepin.async.JobExecutionContext;
import org.candlepin.async.JobExecutionException;
import org.candlepin.common.exceptions.NotFoundException;
import org.candlepin.controller.ManifestManager;
import org.candlepin.model.ImportRecord;
import org.candlepin.model.Owner;
import org.candlepin.model.OwnerCurator;
import org.candlepin.pinsetter.tasks.BaseJobTest;
import org.candlepin.sync.ConflictOverrides;
import org.candlepin.sync.Importer;
import org.candlepin.sync.ImporterException;
import org.candlepin.test.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportJobTest extends BaseJobTest {

    @Mock private ManifestManager manifestManager;
    @Mock private JobExecutionContext ctx;
    @Mock private OwnerCurator ownerCurator;
    private ImportJob job;

    private Owner owner;

    @Before
    public void setup() {
        super.init();
        owner = new Owner("my-test-owner");
        job = new ImportJob(ownerCurator, manifestManager);
        injector.injectMembers(job);
    }

    private Owner createTestOwner(String key, String logLevel) {
        Owner owner = TestUtil.createOwner();

        owner.setId(TestUtil.randomString());
        owner.setKey(key);
        owner.setLogLevel(logLevel);

        return owner;
    }

    @Test
    public void testJobConfigSetOwner() {
        Owner owner = this.createTestOwner("owner_key", "log_level");

        JobConfig config = ImportJob.createJobConfig()
            .setOwner(owner);

        Map<String, String> metadata = config.getJobMetadata();
        JobArguments args = config.getJobArguments();

        assertTrue(metadata.containsKey(ImportJob.OWNER_KEY));
        assertEquals(owner.getKey(), metadata.get(ImportJob.OWNER_KEY));
        assertEquals(owner.getLogLevel(), config.getLogLevel());
        assertEquals(owner.getKey(), args.getAsString(ImportJob.OWNER_KEY));
    }

    @Test
    public void testJobConfigSetUploadFileName() {
        String fileName = "file_name_1";

        JobConfig config = ImportJob.createJobConfig()
            .setUploadedFileName(fileName);

        JobArguments args = config.getJobArguments();

        assertTrue(args.containsKey(ImportJob.UPLOADED_FILE_NAME));
        assertEquals(fileName, args.getAsString(ImportJob.UPLOADED_FILE_NAME));
    }

    @Test
    public void testJobConfigSetStoredFileId() {
        String storedFileId = "stored_file_id_1";

        JobConfig config = ImportJob.createJobConfig()
            .setStoredFileId(storedFileId);

        JobArguments args = config.getJobArguments();

        assertTrue(args.containsKey(ImportJob.STORED_FILE_ID));
        assertEquals(storedFileId, args.getAsString(ImportJob.STORED_FILE_ID));
    }

    @Test
    public void testJobConfigSetConflictOverrides() {
        ConflictOverrides conflictOverrides = new ConflictOverrides(
            Importer.Conflict.DISTRIBUTOR_CONFLICT, Importer.Conflict.MANIFEST_SAME);

        JobConfig config = ImportJob.createJobConfig()
            .setConflictOverrides(conflictOverrides);

        JobArguments args = config.getJobArguments();

        assertTrue(args.containsKey(ImportJob.CONFLICT_OVERRIDES));
        assertArrayEquals(conflictOverrides.asStringArray(),
            args.getAs(ImportJob.CONFLICT_OVERRIDES, String[].class));
    }

    @Test
    public void testValidate() throws JobConfigValidationException {
        Owner owner = this.createTestOwner("owner_key", "log_level");

        JobConfig config = ImportJob.createJobConfig()
            .setOwner(owner)
            .setStoredFileId("stored_field_id_1")
            .setUploadedFileName("upload_file_name_1");

        config.validate();
    }

    // TODO: Update this test to use the JUnit5 exception handling once this branch catches up
    // with master
    @Test
    public void testValidateNoOwner() {
        JobConfig config = ImportJob.createJobConfig()
            .setStoredFileId("stored_field_id_1")
            .setUploadedFileName("upload_file_name_1");

        try {
            config.validate();
            fail("an expected exception was not thrown");
        }
        catch (JobConfigValidationException e) {
            // Pass!
        }
    }

    // TODO: Update this test to use the JUnit5 exception handling once this branch catches up
    // with master
    @Test
    public void testValidateNoStoredFileId() {
        Owner owner = this.createTestOwner("owner_key_1", "log_level_1");

        JobConfig config = ImportJob.createJobConfig()
            .setOwner(owner)
            .setUploadedFileName("upload_file_name_1");

        try {
            config.validate();
            fail("an expected exception was not thrown");
        }
        catch (JobConfigValidationException e) {
            // Pass!
        }
    }

    // TODO: Update this test to use the JUnit5 exception handling once this branch catches up
    // with master
    @Test
    public void testValidateNoUploadedFileName() {
        Owner owner = this.createTestOwner("owner_key_1", "log_level_1");

        JobConfig config = ImportJob.createJobConfig()
            .setOwner(owner)
            .setStoredFileId("stored_field_id_1");

        try {
            config.validate();
            fail("an expected exception was not thrown");
        }
        catch (JobConfigValidationException e) {
            // Pass!
        }
    }

    @Test
    public void ensureJobSuccess() throws Exception {
        String uploadFileName = "upload_file_name_1";
        String storedFieldId = "stored_field_id_1";
        ConflictOverrides conflictOverrides = new ConflictOverrides(
            Importer.Conflict.SIGNATURE_CONFLICT, Importer.Conflict.MANIFEST_OLD);

        ImportRecord importRecord = new ImportRecord(owner);
        importRecord.setFileName(uploadFileName);

        JobConfig jobConfig = ImportJob.createJobConfig()
            .setOwner(owner)
            .setStoredFileId(storedFieldId)
            .setUploadedFileName(uploadFileName)
            .setConflictOverrides(conflictOverrides);

        JobExecutionContext context = mock(JobExecutionContext.class);
        doReturn(jobConfig.getJobArguments()).when(context).getJobArguments();
        doReturn(owner).when(ownerCurator).getByKey(eq("my-test-owner"));
        when(manifestManager.importStoredManifest(eq(owner), eq(storedFieldId), any(ConflictOverrides.class),
            eq(uploadFileName))).thenReturn(importRecord);

        Object actualResult = this.job.execute(context);

        assertEquals(importRecord, actualResult);
    }

    // TODO: Update this test to use the JUnit5 exception handling once this branch catches up
    // with master
    @Test
    public void ensureJobFailure() throws Exception {
        String archiveFilePath = "/path/to/some/file.zip";
        ConflictOverrides co = new ConflictOverrides(Importer.Conflict.SIGNATURE_CONFLICT);
        String uploadedFileName = "test.zip";
        String expectedMessage = "Expected Exception Message";

        JobConfig jobConfig = ImportJob.createJobConfig()
            .setOwner(owner)
            .setUploadedFileName(uploadedFileName)
            .setStoredFileId(archiveFilePath)
            .setConflictOverrides(co);

        doReturn(jobConfig.getJobArguments()).when(ctx).getJobArguments();
        doReturn(owner).when(ownerCurator).getByKey(eq("my-test-owner"));
        when(manifestManager.importStoredManifest(eq(owner), eq(archiveFilePath),
            any(ConflictOverrides.class), eq(uploadedFileName)))
            .thenThrow(new ImporterException(expectedMessage));

        try {
            job.execute(ctx);
            fail("Expected exception to be thrown");
        }
        catch (JobExecutionException e) {
            // Expected this failure
            assertEquals(expectedMessage, e.getMessage());
            assertTrue(e.getCause() instanceof ImporterException);
            verify(manifestManager).recordImportFailure(eq(owner), eq(e.getCause()), eq(uploadedFileName));
        }
    }

    // TODO: Update this test to use the JUnit5 exception handling once this branch catches up
    // with master
    @Test
    public void ensureJobExceptionThrownIfOwnerNotFound() {
        String archiveFilePath = "/path/to/some/file.zip";
        ConflictOverrides co = new ConflictOverrides(Importer.Conflict.SIGNATURE_CONFLICT);
        String uploadedFileName = "test.zip";
        String expectedMessage = String.format("Owner %s was not found.", owner.getKey());

        JobConfig jobConfig = ImportJob.createJobConfig()
            .setOwner(owner)
            .setUploadedFileName(uploadedFileName)
            .setStoredFileId(archiveFilePath)
            .setConflictOverrides(co);

        doReturn(jobConfig.getJobArguments()).when(ctx).getJobArguments();
        doReturn(null).when(ownerCurator).getByKey(eq("my-test-owner"));

        try {
            job.execute(ctx);
            fail("Expected exception not thrown");
        }
        catch (JobExecutionException e) {
            // Expected this failure
            assertEquals(expectedMessage, e.getMessage());
            assertTrue(e.getCause() instanceof NotFoundException);
            verify(manifestManager).recordImportFailure(eq(null), eq(e.getCause()), eq(uploadedFileName));
        }
    }
}
