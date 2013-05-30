package projectZoom.thumbnails;

import java.io.File;
import java.util.List;
import models.ResourceLike;

public abstract class ThumbnailPlugin {
	public abstract List<TempFile> onResourceFound(File file, ResourceLike resource);

}
