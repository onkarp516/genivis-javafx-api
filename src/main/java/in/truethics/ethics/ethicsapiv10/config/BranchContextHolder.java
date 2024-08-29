package in.truethics.ethics.ethicsapiv10.config;


public class BranchContextHolder {

	private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

	public static void setBranchContext(String branchEnum) {
		threadLocal.set(branchEnum);
	}

	public static String getBranchContext() {
		return threadLocal.get();
	}

	public static void clearBranchContext() {
		threadLocal.remove();
	}
}
