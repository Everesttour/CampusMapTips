import { cp, mkdir, readdir, rm } from 'node:fs/promises'
import { dirname, extname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import sharp from 'sharp'

const scriptDirectory = dirname(fileURLToPath(import.meta.url))
const webDirectory = resolve(scriptDirectory, '..')
const resourcesDirectory = resolve(webDirectory, '..', 'src', 'resources')
const publicDirectory = join(webDirectory, 'public')

async function webp(input, output, width, rotateLandscape = false) {
  await mkdir(dirname(output), { recursive: true })
  const metadata = await sharp(input).metadata()
  const shouldRotatePortrait =
    rotateLandscape && metadata.width && metadata.height && metadata.width > metadata.height
  const image = shouldRotatePortrait ? sharp(input).rotate(90) : sharp(input)
  await image
    .resize({ width, withoutEnlargement: true })
    .webp({ quality: 78, effort: 4 })
    .toFile(output)
}

async function compressPhotoSet(sourceDirectory, destinationDirectory) {
  const files = await readdir(sourceDirectory)
  const images = files.filter((file) => extname(file).toLowerCase() === '.jpg')

  for (const image of images) {
    const match = image.match(/^(\d+)_/)
    if (!match) continue
    await webp(join(sourceDirectory, image), join(destinationDirectory, `${match[1]}.webp`), 1000, true)
  }
}

await mkdir(publicDirectory, { recursive: true })
await rm(join(publicDirectory, 'data'), { recursive: true, force: true })
await rm(join(publicDirectory, 'images'), { recursive: true, force: true })

await cp(join(resourcesDirectory, 'data'), join(publicDirectory, 'data'), { recursive: true })
await webp(join(resourcesDirectory, 'images', 'map', 'campus_map.jpg'), join(publicDirectory, 'images', 'map.webp'), 1800)
await compressPhotoSet(join(resourcesDirectory, 'images', 'Buildings'), join(publicDirectory, 'images', 'buildings'))
await compressPhotoSet(join(resourcesDirectory, 'images', 'Facilities'), join(publicDirectory, 'images', 'facilities'))

console.log('Static map, data, and WebP detail images are ready.')
