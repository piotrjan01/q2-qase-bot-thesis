package piotrrr.thesis.gui;

public class MainFrameUpdater implements Runnable {
	
	private MainFrame mf;
	
	public boolean stop = false;
	
	public MainFrameUpdater(MainFrame mf) {
		this.mf = mf;
	}

	@Override
	public void run() {
		while ( ! stop ) {
			try {
				Thread.sleep(300);
				mf.updateDisplayedInfo();
			} catch (InterruptedException e) {
				return;
			}
			
		}
	}

}
