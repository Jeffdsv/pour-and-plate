import { useEffect } from 'react'
import { useMap } from 'react-leaflet'
import L from 'leaflet'
import '@maplibre/maplibre-gl-leaflet'
import 'maplibre-gl/dist/maplibre-gl.css'
import * as maplibregl from 'maplibre-gl'
import workerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?url'


// vite moves files around so maplibre cant find its worker by itself, we tell it where it is
maplibregl.setWorkerUrl(workerUrl)

// a clean map style from openfreemap, free and without an api key
const STYLE_URL = 'https://tiles.openfreemap.org/styles/liberty'

// map details we hide for a cleaner look, matched against the layer names in the style
const HIDDEN = ['rail', 'transit', 'poi', 'aeroway', 'ferry']

// our own colors, close to google maps
const COLORS = {
  background: '#F5F5F2',
  water: '#8CC8F5',
  park: '#B5E3A1',
  building: '#E9E7E3',
}

// draws the map background as a vector map instead of image tiles
export default function VectorBaseMap() {
  const map = useMap()

    useEffect(() => {
    const layer = L.maplibreGL({ style: STYLE_URL }).addTo(map)

    // the credit line in the corner of the map, required by openfreemap and openstreetmap
    const credit =
      '&copy; <a href="https://openfreemap.org">OpenFreeMap</a> ' +
      '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    map.attributionControl.addAttribution(credit)

    // once the style has loaded, switch off the details we do not want
    const maplibre = layer.getMaplibreMap()
        maplibre.on('style.load', () => {
      for (const styleLayer of maplibre.getStyle().layers) {
        const id = styleLayer.id

        // hide railways, bus stops, shop icons and so on
        if (HIDDEN.some((word) => id.includes(word))) {
          maplibre.setLayoutProperty(id, 'visibility', 'none')
          continue
        }

        // 3d buildings are what gives the shadows, we only want flat ones
                // 3d buildings gave the shadows, we keep them but make them flat with our own color
        if (styleLayer.type === 'fill-extrusion') {
          maplibre.setPaintProperty(id, 'fill-extrusion-height', 0)
          maplibre.setPaintProperty(id, 'fill-extrusion-base', 0)
          maplibre.setPaintProperty(id, 'fill-extrusion-color', COLORS.building)
          maplibre.setPaintProperty(id, 'fill-extrusion-vertical-gradient', false)
          continue
        }

        if (styleLayer.type === 'background') {
          maplibre.setPaintProperty(id, 'background-color', COLORS.background)
        }

        // fill layers are the colored areas, we pick the color based on the layer name
        if (styleLayer.type === 'fill') {
          if (id.includes('water')) {
            maplibre.setPaintProperty(id, 'fill-color', COLORS.water)
          } else if (['park', 'wood', 'grass', 'forest'].some((word) => id.includes(word))) {
            maplibre.setPaintProperty(id, 'fill-color', COLORS.park)
          } else if (id.includes('building')) {
            maplibre.setPaintProperty(id, 'fill-color', COLORS.building)
          }
        }
      }
    })

    // cleanup: remove the layer and its credit if this component is ever removed
    return () => {
      layer.remove()
      map.attributionControl.removeAttribution(credit)
    }
  }, [map])

  // draws nothing itself, the layer above does the drawing
  return null
}