import lombok.experimental.Locked;
class LockedPlain {
  private final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
  LockedPlain() {
    super();
  }
  @lombok.experimental.Locked void test() {
    this.lock.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        this.lock.unlock();
      }
  }
  @Locked void test2() {
    this.lock.lock();
    try
      {
        System.out.println("two");
      }
    finally
      {
        this.lock.unlock();
      }
  }
}
class LockedPlainStatic {
  private static final java.util.concurrent.locks.ReentrantLock LOCK = new java.util.concurrent.locks.ReentrantLock();
  <clinit>() {
  }
  LockedPlainStatic() {
    super();
  }
  static @lombok.experimental.Locked void test() {
    LockedPlainStatic.LOCK.lock();
    try
      {
        System.out.println("three");
      }
    finally
      {
        LockedPlainStatic.LOCK.unlock();
      }
  }
  static @Locked void test2() {
    LockedPlainStatic.LOCK.lock();
    try
      {
        System.out.println("four");
      }
    finally
      {
        LockedPlainStatic.LOCK.unlock();
      }
  }
}
class LockedPlainRead {
  private static final java.util.concurrent.locks.ReentrantReadWriteLock LOCK = new java.util.concurrent.locks.ReentrantReadWriteLock();
  <clinit>() {
  }
  LockedPlainRead() {
    super();
  }
  static @lombok.experimental.Locked.Read void test() {
    LockedPlainRead.LOCK.readLock().lock();
    try
      {
        System.out.println("five");
      }
    finally
      {
        LockedPlainRead.LOCK.readLock().unlock();
      }
  }
  static @Locked.Read void test2() {
    LockedPlainRead.LOCK.readLock().lock();
    try
      {
        System.out.println("six");
      }
    finally
      {
        LockedPlainRead.LOCK.readLock().unlock();
      }
  }
}
class LockedPlainWrite {
  private final java.util.concurrent.locks.ReentrantReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
  LockedPlainWrite() {
    super();
  }
  @lombok.experimental.Locked.Write void test() {
    this.lock.writeLock().lock();
    try
      {
        System.out.println("seven");
      }
    finally
      {
        this.lock.writeLock().unlock();
      }
  }
  @Locked.Write void test2() {
    this.lock.writeLock().lock();
    try
      {
        System.out.println("eight");
      }
    finally
      {
        this.lock.writeLock().unlock();
      }
  }
}
