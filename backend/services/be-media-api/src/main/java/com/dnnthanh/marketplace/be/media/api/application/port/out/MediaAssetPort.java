package com.dnnthanh.marketplace.be.media.api.application.port.out;

import com.dnnthanh.marketplace.be.media.api.domain.model.MediaAsset;
import java.util.Optional;

/** Media source-of-truth persistence boundary. */
public interface MediaAssetPort {
    Optional<MediaAsset> findById(String mediaId);

    Optional<MediaAsset> findByChecksum(String checksum);

    MediaAsset save(MediaAsset asset);
}
