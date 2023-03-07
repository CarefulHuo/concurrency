package io.github.carefulhuo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author hwy
 * @version 1.0.0
 * @description 简易独占锁实现
 * <p>
 *     使用同步器实现锁时，需要考虑加锁，释放锁，等待队列，锁是否已存在，是否存在等待获取的锁
 *     还有以及是否响应中断异常、锁获取超时
 * </p>
 * @date 2023/3/716:26
 */
public class Mutex implements Lock {

	/**
	 * 静态内部类实现自定义同步器
 	 */
	private static class Sync extends AbstractQueuedSynchronizer{

		/**
		 * 是否处于占用状态
		 * @return
		 */
		@Override
		protected boolean isHeldExclusively(){
			return getState() == 1;
		}

		/**
		 * 状态为 0 时获取锁
		 * @param acquire
		 * @return
		 */
		@Override
		protected boolean tryAcquire(int acquire){
			if (compareAndSetState(0,1)){
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;
		}

		/**
		 * 状态为 1 时 释放锁
		 * @param release
		 * @return
		 */
		@Override
		protected boolean tryRelease(int release){
			if (getState() == 0){
				throw new IllegalMonitorStateException();
			}
			setExclusiveOwnerThread(null);
			setState(0);
			return true;
		}

		/**
		 * 返回 condition , 每个新的 condition 对象都包含一个新的队列
		 * @return
		 */
		Condition newCondition() {
			return new ConditionObject();
		}

	}

	/** Sync 对象完成加锁，释放锁的所有操作 **/
	private final Sync sync = new Sync();

	/**
	 * 尝试加锁，若失败放入队列，忽略中断异常
	 */
	@Override
	public void lock() {
		sync.acquire(1);
	}

	/**
	 * 尝试加锁，若失败放入队列，抛出中断异常
	 * @throws InterruptedException
	 */
	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	/**
	 * 尝试加锁，成功返回 true 失败返回 false
	 * @return
	 */
	@Override
	public boolean tryLock() {
		return sync.tryAcquire(1);
	}


	/**
	 * 在固定时间内尝试获取锁，成功返回 true 失败或超时返回 false
	 * @param time
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1,unit.toNanos(time));
	}

	/**
	 * 释放锁
	 */
	@Override
	public void unlock() {
		sync.release(1);
	}

	/**
	 * 生成等待队列
	 * @return
	 */
	@Override
	public Condition newCondition() {
		return sync.newCondition();
	}

	/**
	 * 是否存在锁
	 * @return
	 */
	public boolean isLocked(){
		 return sync.isHeldExclusively();
	}

	/**
	 * 是否存在等待获取锁的线程，如果可能存在，返回 true 否则返回 false
	 * @return
	 */
	public boolean hasQueuedThreads(){
		return sync.hasQueuedThreads();
	}
}
