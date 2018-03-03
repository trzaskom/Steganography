import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Created by miki on 2018-01-26.
 */
public class Audio {
    int byteStep = 2;   // one sample written in 16 bits, LSB every 2 bytes


    public byte[] toBytes(File audioFile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        byte[] audioBytes = null;
        try {
            in = new BufferedInputStream(new FileInputStream(audioFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read;
        byte[] buffer = new byte[1024];
        try {
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.flush();
            audioBytes = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioBytes;
    }

    //serching in file header for data chunk ID
    public int findDataOffset(byte[] audioBytes) {
        int i = 4;
        int j;
        byte[] data = new byte[]{0x64, 0x61, 0x74, 0x61};   //"DATA" in ASCII

        while (i <= audioBytes.length) {
            j = i - 4;
            if (Arrays.equals(Arrays.copyOfRange(audioBytes, j, i), data))
                return i + 4;
            i++;
        }
        throw new NoSuchElementException("No subarray matching with \"data\" subchunk descriptor");
    }


    public int findDataLength(byte[] audioBytes, int audioDataOffset) {
        ByteBuffer wrapped = ByteBuffer.wrap(audioBytes, audioDataOffset - 4, audioDataOffset);
        return wrapped.getInt();
    }


    public byte[] embedBits(byte[] audioBytes, byte[] messageBytes, int offset){
        int addition;
        int newLeastSignificantBit;

        for (int i = 0; i < messageBytes.length; ++i) {
            addition = messageBytes[i];
            for (int bit = 7; bit >= 0; --bit, offset +=byteStep) {
                newLeastSignificantBit = (addition >>> bit) & 1;
                audioBytes[offset] = (byte) ((audioBytes[offset] & 0xFE) | newLeastSignificantBit);
            }
        }
        return audioBytes;
    }


    public byte[] encodeMessage(byte[] audioBytes, String messageString) {
        int offset = findDataOffset(audioBytes);
        Message message = new Message();
        byte[] messageBytes = message.toBytes(messageString);
        byte[] messageLengthBytes = message.lengthToBytes(messageBytes.length);

        if (((messageBytes.length + messageLengthBytes.length) * 8 * byteStep) > (findDataLength(audioBytes, offset))) {
            throw new IllegalArgumentException("Size of image is not big enough to encode message!");
        }

        embedBits(audioBytes,messageLengthBytes,offset);
        embedBits(audioBytes,messageBytes,offset + (32*byteStep));

        return audioBytes;
    }


    public byte[] extractBits(byte[] audioBytes) {
        int offset = findDataOffset(audioBytes);
        int messageLength = 0;

        for (int i = offset; i < offset + 32 * byteStep; i += byteStep) {
            messageLength = (messageLength << 1) | (audioBytes[i] & 1);
        }
        offset += 32 * byteStep;
        byte[] messageBytes = new byte[messageLength];

        for (int i = 0; i < messageLength; ++i) {
            for (int j = 0; j < 8; ++j, offset += byteStep) {
                messageBytes[i] = (byte) ((messageBytes[i] << 1) | (audioBytes[offset] & 1));
            }
        }
        return messageBytes;
    }


    public String decodeMessage(byte[] audioBytes) {
        audioBytes = extractBits(audioBytes);
        String message = new String(audioBytes);
        return message;
    }


    public static void write(byte[] audioBytes, File stegoFile) {
        InputStream inputStream = new ByteArrayInputStream(audioBytes);
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, stegoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
