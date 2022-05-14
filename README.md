# Config

Exists in `C:\ProgramData\GERAGHT0\ANDREW_TOOLS\1\folder-tool.properties`

User configuration options:

1) `folder-tool.skipVersionUpdateCheck=true` to disable checks for new versions
2) `folder-tool.disallowOverwrite=true` to disallow overwriting of files if they already exist in the destinations
3) `folder-tool.disableInfoPopupBetweenViews=true` to disable the info popup that appears when switching

# Brief

Okayyyyy so there’s two versions of the app that could possibly be done:

The first version I would put into an app a list of the container names I want to create folders for, e.g.

DVR001 DVR002 DVR003

And from there it will create a folder structure like:

DVR001 - paid/o/videos
- tour/o/videos
  artwork
  gallery

I would then add the corresponding files manual to each folder.

Option 2:
I would create the root folder (e.g. dvr001) and dump all the files in there:
- artwork.jpg
- filename_preview.mp4 (5 of these)
- filename.mp4 (5 of these)
- imagename-100.jpg (6 of these with the numbers going from -100 to -106)

Once they’re in there, I could point the app to the directory where it would know to create the folder structure and put the files in the correct folder.

This is what the file structure would look like with video files added:

DVR001 - paid/o/videos (5 videos that don’t have _preview in the name)  .mp4
- tour/o/videos (5 videos that have _preview in the name)        .mp4
  artwork (artwork.jpg)
gallery (6 gallery images)                 .jpg,.jpeg