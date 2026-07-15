Add-Type -AssemblyName System.Drawing
$resDir = 'D:\Downloads\MyApps\Antigravity_workspace\PayTrack\app\src\main\res'
$origPath = Join-Path $resDir 'drawable\ic_launcher_foreground_original.png'
$fgPath = Join-Path $resDir 'drawable\ic_launcher_foreground.png'

if (-not (Test-Path $origPath)) {
    Rename-Item -Path $fgPath -NewName 'ic_launcher_foreground_original.png'
}

$img = [System.Drawing.Image]::FromFile($origPath)

# Calculate aspect ratio
$imgWidth = $img.Width
$imgHeight = $img.Height
$aspectRatio = $imgWidth / $imgHeight

# Generate padded foreground
$fgSize = 1024
$fgBmp = New-Object System.Drawing.Bitmap($fgSize, $fgSize)
$fgGraph = [System.Drawing.Graphics]::FromImage($fgBmp)
$fgGraph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic

# scale to 100% of canvas for launcher icon
$maxDrawSize = [int]($fgSize * 0.75)

if ($aspectRatio -gt 1) {
    # wider than tall
    $drawWidth = $maxDrawSize
    $drawHeight = [int]($maxDrawSize / $aspectRatio)
} else {
    # taller than wide or square
    $drawHeight = $maxDrawSize
    $drawWidth = [int]($maxDrawSize * $aspectRatio)
}

# Perfectly center horizontally (no shift)
$shiftX = 0
$offsetX = [int](($fgSize - $drawWidth) / 2) + $shiftX
$offsetY = [int](($fgSize - $drawHeight) / 2)
$fgGraph.DrawImage($img, $offsetX, $offsetY, $drawWidth, $drawHeight)

$pngFormat = [System.Drawing.Imaging.ImageFormat]::Png
$fgBmp.Save($fgPath, $pngFormat)

$fgGraph.Dispose()
$fgBmp.Dispose()

# Now generate legacy icons (with white background just in case)
$sizes = @{
    'mdpi' = 48;
    'hdpi' = 72;
    'xhdpi' = 96;
    'xxhdpi' = 144;
    'xxxhdpi' = 192
}

foreach ($density in $sizes.Keys) {
    $size = $sizes[$density]
    $outDir = Join-Path $resDir ('mipmap-' + $density)
    
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $graph = [System.Drawing.Graphics]::FromImage($bmp)
    $graph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    
    # scale to 100% for legacy launcher icon
    $maxDrawSizeLegacy = [int]($size * 1.0)
    
    if ($aspectRatio -gt 1) {
        $drawWidthLegacy = $maxDrawSizeLegacy
        $drawHeightLegacy = [int]($maxDrawSizeLegacy / $aspectRatio)
    } else {
        $drawHeightLegacy = $maxDrawSizeLegacy
        $drawWidthLegacy = [int]($maxDrawSizeLegacy * $aspectRatio)
    }
    
    # Perfectly center legacy icon (no shift)
    $shiftXLegacy = 0
    $offsetXLegacy = [int](($size - $drawWidthLegacy) / 2) + $shiftXLegacy
    $offsetYLegacy = [int](($size - $drawHeightLegacy) / 2)
    
    # Clip legacy icon to a circular shape
    $clipPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $clipPath.AddEllipse(0, 0, $size, $size)
    $graph.SetClip($clipPath)
    
    # Fill background with white
    $bgBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml("#FFFFFF"))
    $graph.FillRectangle($bgBrush, 0, 0, $size, $size)
    $bgBrush.Dispose()
    
    $graph.DrawImage($img, $offsetXLegacy, $offsetYLegacy, $drawWidthLegacy, $drawHeightLegacy)
    
    $icLauncherPath = Join-Path $outDir 'ic_launcher.png'
    $bmp.Save($icLauncherPath, $pngFormat)
    
    $icLauncherRoundPath = Join-Path $outDir 'ic_launcher_round.png'
    $bmp.Save($icLauncherRoundPath, $pngFormat)
    
    $graph.Dispose()
    $bmp.Dispose()
}

$img.Dispose()
Write-Host 'Icons regenerated with proportional padding!'
