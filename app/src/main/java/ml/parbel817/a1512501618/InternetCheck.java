package ml.parbel817.a1512501618;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by ipin on 9/25/2018.
 */

class InternetCheck extends AsyncTask<Void,Void,Boolean> {

    private Consumer mConsumer;
    public interface Consumer {
        void accept(Boolean internet);
    }

    public InternetCheck(Consumer consumer) {
        mConsumer = consumer; execute();
    }

    // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
            sock.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean internet) {
        mConsumer.accept(internet);
    }
}
