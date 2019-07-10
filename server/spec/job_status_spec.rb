require 'spec_helper'
require 'candlepin_scenarios'

describe 'Job Status' do
  include CandlepinMethods

  before(:each) do
    @owner = create_owner(random_string("test_owner"))
    @owner2 = create_owner(random_string("test_owner_2"))
    @user = user_client(@owner, random_string("test_user"))
    @monitoring = create_product

    create_pool_and_subscription(@owner['key'], @monitoring.id, 4)
  end

  after(:each) do
    @cp.set_async_scheduler_status(true)
  end

  it 'should find an empty list if the owner key is wrong' do
    @cp.list_jobs('totaly_made_up').should be_empty
  end

  it 'should return an error if no owner key is supplied' do
    lambda do
      @cp.list_jobs('')
    end.should raise_exception(RestClient::BadRequest)
  end

  it 'should cancel a job' do
    @cp.set_async_scheduler_status(false)
    job = @cp.autoheal_org(@owner['key'])

    #make sure we see a job waiting to go
    status = @cp.get_async_job(job['id'])
    status['state'].should == 'QUEUED'

    @cp.cancel_async_job(job['id'])
    #make sure we see a job canceled
    status = @cp.get_async_job(job['id'])
    status['state'].should == 'CANCELED'

    @cp.set_async_scheduler_status(true)
    sleep 1 #let the job queue drain..
    #make sure job didn't flip to FINISHED
    status = @cp.get_async_job(job['id'])
    status['state'].should == 'CANCELED'
  end

  it 'should allow admin to view any job status' do
    job = @cp.autoheal_org(@owner['key'])
    wait_for_async_job(job['id'], 15)
    status = @cp.get_async_job(job['id'])
    status['id'].should == job['id']
  end

  it 'should allow user to view status of own job' do
    job = @user.autoheal_org(@owner['key'])
    wait_for_async_job(job['id'], 15)
    status = @user.get_async_job(job['id'])
    status['id'].should == job['id']
  end

  it 'should allow user to view job status of consumer in managed org' do
    system = consumer_client(@user, 's1')
    job = system.consume_product(@monitoring.id, { :async => true })
    status = @user.get_job(job['id'])
    status['id'].should == job['id']
    # wait for job to complete, or test clean up will conflict with the asynchronous job.
    wait_for_job(status['id'], 15)
  end

  it 'should not allow user to cancel job from another user' do
    other_user = user_client(@owner,  random_string("other_user"))
    job = @user.autoheal_org(@owner['key'])
    lambda do
      other_user.cancel_async_job(job['id'])
    end.should raise_exception(RestClient::Forbidden)
  end

  it 'should allow user to cancel a job it initiated' do
    @cp.set_async_scheduler_status(false)
    job = @user.autoheal_org(@owner['key'])
    #make sure we see a job waiting to go
    status = @cp.get_async_job(job['id'])
    status['state'].should == 'QUEUED'

    @user.cancel_async_job(job['id'])
    #make sure we see a job canceled
    status = @cp.get_async_job(job['id'])
    status['state'].should == 'CANCELED'

    @cp.set_async_scheduler_status(true)
    sleep 1 #let the job queue drain..
    #make sure job didn't flip to FINISHED
    status = @cp.get_async_job(job['id'])
    status['state'].should == 'CANCELED'
  end

  it 'should not allow user to cancel a job it did not initiate' do
    system = consumer_client(@user, 'system7')
    job = system.consume_product(@monitoring.id, { :async => true })
    status = system.get_job(job['id'])
    lambda do
      @user.cancel_job(job['id'])
    end.should raise_exception(RestClient::Forbidden)
  end

  it 'should not allow user to view job status outside of managed org' do
    other_user = user_client(@owner2, random_string("other_user"))
    system = consumer_client(other_user, random_string("another_system"))
    job = system.consume_product(@monitoring.id, { :async => true })
    # wait for job to complete, or test clean up will conflict with the asynchronous job.
    wait_for_job(job['id'], 15)
    lambda do
      @user.get_job(job['id'])
    end.should raise_exception(RestClient::Forbidden)

  end

  it 'should allow consumer to view status of own job' do
    system = consumer_client(@user, 'system7')
    job = system.consume_product(@monitoring.id, { :async => true })
    status = system.get_job(job['id'])
    status['id'].should eq(job['id'])
    # wait for job to complete, or test clean up will conflict with the asynchronous job.
    wait_for_job(status['id'], 15)
  end

  it 'should not allow consumer to access another consumers job status' do
    system1 = consumer_client(@user, 's1')
    system2 = consumer_client(@user, 's2')

    job = system1.consume_product(@monitoring.id, { :async => true })
    status = system1.get_job(job['id'])
    # wait for job to complete, or test clean up will conflict with the asynchronous job.
    wait_for_job(status['id'], 15)

    lambda do
      system2.get_job(job['id'])
    end.should raise_exception(RestClient::Forbidden)
  end

  it 'should allow consumer to cancel own job' do
    system = consumer_client(@user, 'system7')
    job = system.consume_product(@monitoring.id, { :async => true })
    status = system.get_job(job['id'])
    system.cancel_job(job['id'])
    # wait for job to complete, or test clean up will conflict with the asynchronous job.
    wait_for_job(status['id'], 15)
  end

  it 'should not allow consumer to cancel another consumers job' do
    system1 = consumer_client(@user, 's1')
    system2 = consumer_client(@user, 's2')

    job = system1.consume_product(@monitoring.id, { :async => true })
    status = system1.get_job(job['id'])
    lambda do
      system2.cancel_job(job['id'])
    end.should raise_exception(RestClient::Forbidden)
  end


end

