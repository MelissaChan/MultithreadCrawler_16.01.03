package Text;


public class ThreadText  extends Thread{
	int count = 1,number;
	
	public ThreadText(int num) {
		number = num;
		System.out.println("创建线程" + number);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			System.out.println("线程"+number+": 计数："+count);
			if(++count == 6) return;
		}
	}

	public static void main(String[] args) {
		for(int i = 0; i < 6; i++){
			new ThreadText(i+1).start();
		}
	}

}
