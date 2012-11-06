package transformation;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

public class ImageRotation {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			if (line.startsWith("Content-Type: image")) {
				StringBuilder imageBuilder = new StringBuilder();
				while (!(line = br.readLine()).startsWith("--")) {
					if (line.lastIndexOf(':') != -1) {
						System.out.println(line);
					} else {
						imageBuilder.append(line);
					}
				}
				String body = imageBuilder.toString();
				byte[] decodedImage = Base64.decodeBase64(body);
				File file = new File("tmpImage");
				FileWriter tmp = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(tmp);
				out.write(new String(decodedImage));
				out.close();
				rotate(file);
				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in));
				StringBuilder strLine = new StringBuilder();
				String aux;
				while ((aux = br2.readLine()) != null) {
					strLine.append(aux);
				}
				if (strLine.length() == 0) {
					System.out.println("Error ");
				}
				in.close();
				String result = Base64.encodeBase64String(strLine.toString()
						.getBytes());
				char[] array = result.toCharArray();
				for ( int i = 0 ; i < array.length ; i++) {
					if ( i > 0 && i % 77 == 0 ) {
						System.out.println();
					}
					System.out.println(array[i]);
				}
				System.out.println();
				file.delete();
			}
		}
	}

	static void rotate(File file) throws IOException {
		BufferedImage image = null;
		image = ImageIO.read(file);
		if (image == null) {
			return;
		}
		AffineTransform rotationTransform = new AffineTransform();
		rotationTransform.setToTranslation(
				0.5 * Math.max(image.getWidth(), image.getHeight()),
				0.5 * Math.max(image.getWidth(), image.getHeight()));
		rotationTransform.rotate(Math.toRadians(180));
		if (image.getWidth() > image.getHeight()) {
			rotationTransform.translate(
					-0.5 * Math.max(image.getWidth(), image.getHeight()), -0.5
							* Math.max(image.getWidth(), image.getHeight())
							+ Math.abs(image.getWidth() - image.getHeight()));
		} else {
			rotationTransform.translate(
					-0.5 * Math.max(image.getWidth(), image.getHeight())
							+ Math.abs(image.getWidth() - image.getHeight()),
					-0.5 * Math.max(image.getWidth(), image.getHeight()));
		}
		AffineTransformOp op = new AffineTransformOp(rotationTransform,
				AffineTransformOp.TYPE_BILINEAR);
		BufferedImage result = op.filter(image, null);
		ImageIO.write(result, "png", file);
	}
}
