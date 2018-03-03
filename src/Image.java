import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/**
 * Created by miki on 2018-01-26.
 */
public class Image {


    public BufferedImage read(File imageFile) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.out.println("Couldn't read file!");
        }
        return image;
    }


    public byte[] toBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte imageBytes = (DataBufferByte) raster.getDataBuffer();
        return imageBytes.getData();
    }


    public byte[] embedBits(byte[] imageBytes, byte[] messageBytes, int offset) {
        int addition;
        int newLeastSignificantBit;

        for (int i = 0; i < messageBytes.length; ++i) {
            addition = messageBytes[i];
            for (int bit = 7; bit >= 0; --bit, ++offset) {
                newLeastSignificantBit = (addition >>> bit) & 1;
                imageBytes[offset] = (byte) ((imageBytes[offset] & 0xFE) | newLeastSignificantBit);
            }
        }
        return imageBytes;
    }


    public BufferedImage encodeMessage(BufferedImage image, String messageString) {
        Message message = new Message();
        byte[] imageBytes = toBytes(image);
        byte[] messageBytes = message.toBytes(messageString);
        byte[] messageLengthBytes = message.lengthToBytes(messageBytes.length);

        if (((messageBytes.length + messageLengthBytes.length) * 8) > imageBytes.length) {
            throw new IllegalArgumentException("Size of image is not big enough to encode message!");
        }

        embedBits(imageBytes, messageLengthBytes, 0);
        embedBits(imageBytes, messageBytes, 32);

        return image;
    }


    public byte[] extractBits(byte[] imageBytes) {
        int offset = 32;
        int messageLength = 0;

        for (int i = 0; i < 32; ++i) {
            messageLength = (messageLength << 1) | (imageBytes[i] & 1);
        }
        byte[] messageBytes = new byte[messageLength];

        for (int i = 0; i < messageLength; ++i) {
            for (int j = 0; j < 8; ++j, ++offset) {
                messageBytes[i] = (byte) ((messageBytes[i] << 1) | (imageBytes[offset] & 1));
            }
        }
        return messageBytes;
    }


    public String decodeMessage(BufferedImage image) {
        byte[] imageBytes = toBytes(image);
        byte[] messageBytes = extractBits(imageBytes);
        String message = new String(messageBytes);
        return message;
    }


    public static void write(BufferedImage image, File stegoFile) {
        try {
            ImageIO.write(image, "png", stegoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
