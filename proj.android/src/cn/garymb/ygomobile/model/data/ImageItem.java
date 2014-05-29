package cn.garymb.ygomobile.model.data;

/**
 * @author mabin
 *
 */
public class ImageItem {

	public String id;
	
	public String urlSegment;
	
	public int width;
	
	public int height;

	public ImageItem(String id, int height, int width) {
		this.id = id;
		this.width = width;
		this.height = height;
	}
	
	public ImageItem(String id, int height, int width, String urlSegment) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.urlSegment = urlSegment;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ImageItem))
			return false;
		
		ImageItem target = (ImageItem) o;
		if (this.id != null && this.id.equals(target.id))
			return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
