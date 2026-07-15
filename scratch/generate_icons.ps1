Add-Type -AssemblyName System.Drawing
$sourceImagePath = 'D:\Downloads\MyApps\Antigravity_workspace\PayTrack\app\src\main\res\drawable\ic_launcher_foreground.png'
$baseDir = 'D:\Downloads\MyApps\Antigravity_workspace\PayTrack\app\src\main\res'
$sizes = @{
    'mdpi' = 48;
    'hdpi' = 72;
    'xhdpi' = 96;
    'xxhdpi' = 144;
    'xxxhdpi' = 192
}

try {
    $img = [System.Drawing.Image]::FromFile($sourceImagePath)
    
    foreach ($density in $sizes.Keys) {
        $size = $sizes[$density]
        $outDir = Join-Path $baseDir ('mipmap-' + $density)
        if (-not (Test-Path -Path $outDir)) {
            New-Item -ItemType Directory -Path $outDir | Out-Null
        }
        
        $bmp = New-Object System.Drawing.Bitmap($size, $size)
        $graph = [System.Drawing.Graphics]::FromImage($bmp)
        $graph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graph.DrawImage($img, 0, 0, $size, $size)
        
        $pngFormat = [System.Drawing.Imaging.ImageFormat]::Png
        
        $icLauncherPath = Join-Path $outDir 'ic_launcher.png'
        $bmp.Save($icLauncherPath, $pngFormat)
        
        $icLauncherRoundPath = Join-Path $outDir 'ic_launcher_round.png'
        $bmp.Save($icLauncherRoundPath, $pngFormat)
        
        $graph.Dispose()
        $bmp.Dispose()
    }
    $img.Dispose()
    Write-Host 'Successfully generated all legacy mipmap icons!'
} catch {
    Write-Host 'Error:' $_.Exception.Message
}
