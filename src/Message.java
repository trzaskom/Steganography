/**
 * Created by miki on 2018-01-26.
 */
public class Message {


    public byte[] toBytes(String message) {
        byte[] messageBytes = message.getBytes();
        return messageBytes;
    }


    public byte[] lengthToBytes(int length) {
        byte[] messageLengthBytes = new byte[4];
        messageLengthBytes[0] = (byte) ((length & 0xFF000000) >>> 24);
        messageLengthBytes[1] = (byte) ((length & 0x00FF0000) >>> 16);
        messageLengthBytes[2] = (byte) ((length & 0x0000FF00) >>> 8);
        messageLengthBytes[3] = (byte) ((length & 0x000000FF));
        return messageLengthBytes;
    }
}
