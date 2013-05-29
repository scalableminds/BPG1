package projectZoom.thumbnails;

import java.io.File;
import java.util.List;
import models.ResourceInfo;

public abstract class ThumbnailPlugin {
	public abstract List<TempFile> onResourceFound(File resource, ResourceInfo ressourceInfo);

}
